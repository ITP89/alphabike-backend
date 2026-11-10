package com.alphabike.backend;

import com.alphabike.backend.security.JwtTokenProvider;
import com.alphabike.backend.usuario.Usuario;
import com.alphabike.backend.usuario.UsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class Avance2IntegrationTest {
    @LocalServerPort int port;
    @Autowired ObjectMapper mapper;
    @Autowired UsuarioRepository usuarios;
    @Autowired PasswordEncoder passwords;
    @Autowired JwtTokenProvider tokens;
    final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    String admin, owner, other, staff;

    @BeforeEach
    void identities() {
        admin = identity(Usuario.Rol.ADMIN);
        owner = identity(Usuario.Rol.CLIENTE);
        other = identity(Usuario.Rol.CLIENTE);
        staff = identity(Usuario.Rol.ENCARGADO);
    }

    String identity(Usuario.Rol rol) {
        String email = UUID.randomUUID() + "@test.invalid";
        usuarios.save(Usuario.builder().nombre("Integration " + rol).email(email)
                .telefono("999999999").passwordHash(passwords.encode("Integration123!"))
                .rol(rol).estado(Usuario.Estado.ACTIVO).emailVerificado(true).build());
        return tokens.generateToken(email, rol.name());
    }

    record Result(int status, JsonNode json) {
        JsonNode data() { return json.path("data"); }
        String id() { return data().path("id").asText(); }
    }

    Result request(String method, String path, Object body, String token) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1" + path))
                .timeout(Duration.ofSeconds(10)).header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token);
        builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)));
        var response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        return new Result(response.statusCode(), mapper.readTree(response.body()));
    }

    String service() throws Exception {
        var response = request("POST", "/servicios", Map.of("nombre", "Servicio " + UUID.randomUUID(),
                "descripcion", "Prueba integrada", "precioBase", 50, "duracionMin", 60), admin);
        assertThat(response.status()).isEqualTo(200);
        return response.id();
    }

    String appointment() throws Exception {
        var response = request("POST", "/citas", Map.of("servicioId", service(),
                "fecha", LocalDate.now().plusDays(2).toString(), "hora", "10:00:00",
                "biciDescripcion", "Bicicleta de prueba"), owner);
        assertThat(response.status()).isEqualTo(200);
        return response.id();
    }

    class SocketProbe implements WebSocket.Listener, AutoCloseable {
        final BlockingQueue<JsonNode> messages = new LinkedBlockingQueue<>();
        final CompletableFuture<Integer> closed = new CompletableFuture<>();
        final StringBuilder partial = new StringBuilder();
        WebSocket socket;
        SocketProbe(String token) throws Exception {
            socket = http.newWebSocketBuilder().header("Origin", "http://localhost:5173")
                    .buildAsync(URI.create("ws://localhost:" + port + "/ws/citas"), this).get(5, TimeUnit.SECONDS);
            socket.sendText(mapper.writeValueAsString(Map.of("type", "AUTH", "token", token)), true).join();
        }
        @Override public void onOpen(WebSocket ws) { ws.request(1); }
        @Override public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
            partial.append(data);
            if (last) {
                try { messages.add(mapper.readTree(partial.toString())); }
                catch (Exception ex) { closed.completeExceptionally(ex); }
                partial.setLength(0);
            }
            ws.request(1);
            return CompletableFuture.completedFuture(null);
        }
        @Override public CompletionStage<?> onClose(WebSocket ws, int status, String reason) {
            closed.complete(status);
            return CompletableFuture.completedFuture(null);
        }
        JsonNode next() throws InterruptedException {
            JsonNode event = messages.poll(5, TimeUnit.SECONDS);
            assertThat(event).as("WebSocket event within five seconds").isNotNull();
            return event;
        }
        @Override public void close() { socket.abort(); }
    }

    @Test
    void websocketIsBidirectionalAndIsolatesCustomers() throws Exception {
        try (var client = new SocketProbe(owner); var employee = new SocketProbe(staff); var stranger = new SocketProbe(other)) {
            assertThat(client.next().path("type").asText()).isEqualTo("READY");
            employee.next(); stranger.next();
            client.socket.sendText("{\"type\":\"SYNC\"}", true).join();
            assertThat(client.next().path("type").asText()).isEqualTo("CITAS_SYNC");
            client.socket.sendText("{\"type\":\"PING\"}", true).join();
            assertThat(client.next().path("type").asText()).isEqualTo("PONG");
            String id = appointment();
            assertThat(client.next().path("citaId").asText()).isEqualTo(id);
            employee.next();
            var changed = request("PATCH", "/citas/" + id + "/estado?estado=EN_PROCESO", null, staff);
            assertThat(changed.status()).isEqualTo(200);
            assertThat(client.next().path("estado").asText()).isEqualTo("EN_PROCESO");
            assertThat(employee.next().path("estado").asText()).isEqualTo("EN_PROCESO");
            assertThat(stranger.messages.poll(300, TimeUnit.MILLISECONDS)).isNull();
        }
    }

    @Test
    void websocketRejectsInvalidTokenAndForeignOrigin() throws Exception {
        try (var invalid = new SocketProbe("invalid-token")) {
            assertThat(invalid.closed.get(5, TimeUnit.SECONDS)).isEqualTo(1008);
            assertThat(invalid.messages).isEmpty();
        }
        var rejected = http.newWebSocketBuilder().header("Origin", "https://untrusted.invalid")
                .buildAsync(URI.create("ws://localhost:" + port + "/ws/citas"), new WebSocket.Listener() {});
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> rejected.get(5, TimeUnit.SECONDS))
                .hasCauseInstanceOf(WebSocketHandshakeException.class);
    }

    @Test
    void completeServiceCrudAndSupplierRoles() throws Exception {
        String id = service();
        assertThat(request("GET", "/servicios/" + id, null, owner).status()).isEqualTo(200);
        assertThat(request("PUT", "/servicios/" + id, Map.of("nombre", "Editado " + id,
                "descripcion", "Actualizado", "precioBase", 75, "duracionMin", 45), admin).status()).isEqualTo(200);
        assertThat(request("GET", "/servicios/" + id, null, owner).data().path("precioBase").asInt()).isEqualTo(75);
        assertThat(request("DELETE", "/servicios/" + id, null, admin).status()).isEqualTo(200);
        assertThat(request("GET", "/servicios/" + id, null, owner).status()).isEqualTo(404);
        assertThat(request("GET", "/proveedores", null, staff).status()).isEqualTo(200);
        assertThat(request("GET", "/proveedores", null, owner).status()).isEqualTo(403);
        assertThat(request("POST", "/servicios", Map.of("nombre", ""), admin).status()).isEqualTo(400);
    }

    @Test
    void acceptedQuotationCanBePaidAndCannotBeRewritten() throws Exception {
        String cita = appointment();
        var quote = request("POST", "/cotizaciones/cita/" + cita,
                Map.of("descripcion", "Revision", "monto", 50), admin);
        assertThat(quote.status()).isEqualTo(200);
        assertThat(request("GET", "/cotizaciones/" + quote.id(), null, owner).status()).isEqualTo(200);
        assertThat(request("GET", "/cotizaciones/" + quote.id(), null, other).status()).isEqualTo(403);
        assertThat(request("PATCH", "/cotizaciones/" + quote.id() + "/estado?estado=ACEPTADA", null, owner).status()).isEqualTo(200);
        var payload = Map.of("referenciaTipo", "COTIZACION", "referenciaId", quote.id(), "monto", 50, "metodoPago", "EFECTIVO");
        assertThat(request("POST", "/pagos", payload, staff).status()).isEqualTo(200);
        assertThat(request("POST", "/pagos", payload, staff).status()).isEqualTo(400);
        assertThat(request("POST", "/cotizaciones/cita/" + cita,
                Map.of("descripcion", "Cambio indebido", "monto", 60), admin).status()).isEqualTo(400);
    }

    @Test
    void quotationPaymentPublishesCommittedAppointmentState() throws Exception {
        String cita = appointment();
        var quote = request("POST", "/cotizaciones/cita/" + cita,
                Map.of("descripcion", "Revision", "monto", 50), admin);
        assertThat(quote.status()).isEqualTo(200);
        try (var client = new SocketProbe(owner); var stranger = new SocketProbe(other)) {
            assertThat(client.next().path("type").asText()).isEqualTo("READY");
            stranger.next();
            assertThat(request("POST", "/pagos", Map.of("referenciaTipo", "COTIZACION",
                    "referenciaId", quote.id(), "monto", 50, "metodoPago", "EFECTIVO"), staff).status()).isEqualTo(200);
            JsonNode event = client.next();
            assertThat(event.path("type").asText()).isEqualTo("CITA_UPDATED");
            assertThat(event.path("citaId").asText()).isEqualTo(cita);
            assertThat(event.path("estado").asText()).isEqualTo("COMPLETADO");
            assertThat(request("GET", "/citas/" + cita, null, owner).data().path("estado").asText()).isEqualTo("COMPLETADO");
            assertThat(stranger.messages.poll(300, TimeUnit.MILLISECONDS)).isNull();
        }
    }

    @Test
    void cancelledAppointmentRejectsQuotationPaymentAndReopening() throws Exception {
        String cita = appointment();
        var quote = request("POST", "/cotizaciones/cita/" + cita,
                Map.of("descripcion", "Revision", "monto", 50), admin);
        assertThat(quote.status()).isEqualTo(200);
        assertThat(request("DELETE", "/citas/" + cita, null, owner).status()).isEqualTo(200);
        assertThat(request("POST", "/cotizaciones/cita/" + cita,
                Map.of("descripcion", "Cambio", "monto", 50), admin).status()).isEqualTo(400);
        assertThat(request("PATCH", "/cotizaciones/" + quote.id() + "/estado?estado=ACEPTADA", null, owner).status()).isEqualTo(400);
        assertThat(request("POST", "/pagos", Map.of("referenciaTipo", "COTIZACION",
                "referenciaId", quote.id(), "monto", 50, "metodoPago", "EFECTIVO"), staff).status()).isEqualTo(400);
        assertThat(request("PATCH", "/citas/" + cita + "/estado?estado=EN_PROCESO", null, staff).status()).isEqualTo(400);
        assertThat(request("GET", "/citas/" + cita, null, owner).data().path("estado").asText()).isEqualTo("CANCELADO");
        assertThat(request("GET", "/pagos/referencia/" + quote.id(), null, admin).data()).isEmpty();
    }

    @Test
    void serverValidatesServiceLimitsAndBicycleDescription() throws Exception {
        assertThat(request("POST", "/servicios", Map.of("nombre", "x".repeat(256),
                "precioBase", 50, "duracionMin", 30), admin).status()).isEqualTo(400);
        assertThat(request("POST", "/servicios", Map.of("nombre", "Precision " + UUID.randomUUID(),
                "precioBase", 50.123, "duracionMin", 30), admin).status()).isEqualTo(400);
        assertThat(request("POST", "/citas", Map.of("servicioId", service(),
                "fecha", LocalDate.now().plusDays(2).toString(), "hora", "10:00:00",
                "biciDescripcion", ""), owner).status()).isEqualTo(400);
    }

    @Test
    void invalidParametersAreClientErrorsAndDoNotChangeStock() throws Exception {
        String category = request("POST", "/categorias", Map.of("nombre", "Parametros " + UUID.randomUUID()), admin).id();
        String product = request("POST", "/productos", Map.of("nombre", "Stock valido", "marca", "Test",
                "precio", 40, "stock", 10, "categoriaId", category), admin).id();
        for (String suffix : new String[]{"?stock=abc", "", "?stock=-1", "?stock=1.5", "?stock=2147483648"}) {
            var result = request("PATCH", "/productos/" + product + "/stock" + suffix, null, admin);
            assertThat(result.status()).as(suffix).isEqualTo(400);
        }
        assertThat(request("GET", "/productos/" + product, null, owner).data().path("stock").asInt()).isEqualTo(10);
    }

    @Test
    void catalogAndQuoteValidationRejectsOversizedValuesBeforePersisting() throws Exception {
        var category = request("POST", "/categorias", Map.of("nombre", "Valida " + UUID.randomUUID()), admin);
        assertThat(category.status()).isEqualTo(200);
        assertThat(request("PUT", "/categorias/" + category.id(), Map.of("nombre", "x".repeat(256)), admin).status()).isEqualTo(400);
        assertThat(request("POST", "/categorias", Map.of("nombre", "x".repeat(256)), admin).status()).isEqualTo(400);
        for (Object price : new Object[]{40.001, 100000000}) {
            assertThat(request("POST", "/productos", Map.of("nombre", "Invalido", "marca", "Test",
                    "precio", price, "stock", 10, "categoriaId", category.id()), admin).status()).isEqualTo(400);
        }
        assertThat(request("POST", "/productos", Map.of("nombre", "x".repeat(256), "marca", "Test",
                "precio", 40, "stock", 10, "categoriaId", category.id()), admin).status()).isEqualTo(400);
        String cita = appointment();
        assertThat(request("POST", "/cotizaciones/cita/" + cita,
                Map.of("descripcion", "x".repeat(256), "monto", 40), admin).status()).isEqualTo(400);
        assertThat(request("POST", "/cotizaciones/cita/" + cita,
                Map.of("descripcion", "Revision", "monto", 40.001), admin).status()).isEqualTo(400);
        assertThat(request("POST", "/pagos", Map.of("referenciaTipo", "COTIZACION", "referenciaId", "no-existe",
                "monto", 40.001, "metodoPago", "EFECTIVO"), admin).status()).isEqualTo(400);
    }

    @Test
    void genericCancellationRestoresStockOnlyOnce() throws Exception {
        String category = request("POST", "/categorias", Map.of("nombre", "Categoria " + UUID.randomUUID()), admin).id();
        String product = request("POST", "/productos", Map.of("nombre", "Producto integrado", "marca", "Test",
                "precio", 40, "stock", 10, "categoriaId", category), admin).id();
        var order = request("POST", "/pedidos", Map.of("tipoEntrega", "RECOJO_TIENDA",
                "detalles", new Object[]{ Map.of("productoId", product, "cantidad", 2) }), owner);
        assertThat(order.status()).isEqualTo(200);
        assertThat(request("GET", "/productos/" + product, null, owner).data().path("stock").asInt()).isEqualTo(8);
        assertThat(request("PATCH", "/pedidos/" + order.id() + "/estado?estado=CANCELADO", null, admin).status()).isEqualTo(200);
        assertThat(request("PATCH", "/pedidos/" + order.id() + "/cancelar", null, owner).status()).isEqualTo(200);
        assertThat(request("GET", "/productos/" + product, null, owner).data().path("stock").asInt()).isEqualTo(10);
        assertThat(request("GET", "/pedidos/" + order.id(), null, owner).data().path("estado").asText()).isEqualTo("CANCELADO");
        var paid = request("POST", "/pedidos", Map.of("tipoEntrega", "RECOJO_TIENDA",
                "detalles", new Object[]{ Map.of("productoId", product, "cantidad", 1) }), owner);
        assertThat(request("POST", "/pagos", Map.of("referenciaTipo", "PEDIDO", "referenciaId", paid.id(),
                "monto", 40, "metodoPago", "EFECTIVO"), admin).status()).isEqualTo(200);
        String detail = paid.data().path("detalles").get(0).path("id").asText();
        assertThat(request("PATCH", "/pedidos/" + paid.id() + "/detalle/" + detail + "?precioAcordado=45", null, admin).status()).isEqualTo(400);
        assertThat(request("PATCH", "/pedidos/" + paid.id() + "/estado?estado=CANCELADO", null, admin).status()).isEqualTo(400);
        assertThat(request("PATCH", "/pedidos/" + paid.id() + "/estado?estado=EN_PREPARACION", null, admin).status()).isEqualTo(200);
        assertThat(request("GET", "/pedidos/" + paid.id(), null, owner).data().path("estadoPago").asText()).isEqualTo("PAGADO");
    }
    @Test
    void invalidOrderLinesAndMissingDeliveryAddressDoNotReserveStock() throws Exception {
        String category = request("POST", "/categorias", Map.of("nombre", "Entrega " + UUID.randomUUID()), admin).id();
        String product = request("POST", "/productos", Map.of("nombre", "Producto entrega", "marca", "Test",
                "precio", 40, "stock", 10, "categoriaId", category), admin).id();
        Object[] lines = {Map.of("productoId", product, "cantidad", 1)};
        assertThat(request("POST", "/pedidos", Map.of("tipoEntrega", "RECOJO_TIENDA",
                "detalles", java.util.Arrays.asList((Object) null)), owner).status()).isEqualTo(400);
        for (String type : new String[]{"DELIVERY_LIMA", "ENVIO_PROVINCIA"}) {
            assertThat(request("POST", "/pedidos", Map.of("tipoEntrega", type, "detalles", lines), owner).status()).isEqualTo(400);
            assertThat(request("POST", "/pedidos", Map.of("tipoEntrega", type, "direccionEntrega", "   ", "detalles", lines), owner).status()).isEqualTo(400);
        }
        assertThat(request("GET", "/productos/" + product, null, owner).data().path("stock").asInt()).isEqualTo(10);
        assertThat(request("GET", "/pedidos/mios", null, owner).data()).isEmpty();
        assertThat(request("POST", "/pedidos", Map.of("tipoEntrega", "RECOJO_TIENDA", "detalles", lines), owner).status()).isEqualTo(200);
        var delivery = request("POST", "/pedidos", Map.of("tipoEntrega", "DELIVERY_LIMA",
                "direccionEntrega", "  Av. Prueba 123  ", "detalles", lines), owner);
        assertThat(delivery.status()).isEqualTo(200);
        assertThat(delivery.data().path("direccionEntrega").asText()).isEqualTo("Av. Prueba 123");
        assertThat(delivery.data().path("total").decimalValue()).isEqualByComparingTo("50.00");
    }

    @Test
    void negotiatedPriceRejectsPrecisionAndOverflowWithoutChangingTheOrder() throws Exception {
        String category = request("POST", "/categorias", Map.of("nombre", "Importes " + UUID.randomUUID()), admin).id();
        String product = request("POST", "/productos", Map.of("nombre", "Producto importes", "marca", "Test",
                "precio", 40, "stock", 10, "categoriaId", category), admin).id();
        var order = request("POST", "/pedidos", Map.of("tipoEntrega", "RECOJO_TIENDA",
                "detalles", new Object[]{Map.of("productoId", product, "cantidad", 2)}), owner);
        assertThat(order.status()).isEqualTo(200);
        String detail = order.data().path("detalles").get(0).path("id").asText();
        String path = "/pedidos/" + order.id() + "/detalle/" + detail + "?precioAcordado=";
        for (String price : new String[]{"40.999", "100000000", "50000000", "-1", "0"}) {
            assertThat(request("PATCH", path + price, null, admin).status()).as(price).isEqualTo(400);
            var persisted = request("GET", "/pedidos/" + order.id(), null, owner).data();
            assertThat(persisted.path("total").decimalValue()).isEqualByComparingTo("80.00");
            assertThat(persisted.path("detalles").get(0).path("precioAcordado").decimalValue()).isEqualByComparingTo("40.00");
        }
        assertThat(request("PATCH", path + "45.25", null, admin).status()).isEqualTo(200);
        assertThat(request("GET", "/pedidos/" + order.id(), null, owner).data().path("total").decimalValue()).isEqualByComparingTo("90.50");
    }

    @Test
    void excessiveCalculatedAmountsAndQuantityOverflowRollBackTheReservation() throws Exception {
        String category = request("POST", "/categorias", Map.of("nombre", "Limites " + UUID.randomUUID()), admin).id();
        String product = request("POST", "/productos", Map.of("nombre", "Producto limite", "marca", "Test",
                "precio", 60000000, "stock", 10, "categoriaId", category), admin).id();
        for (Object[] lines : new Object[][]{
                {Map.of("productoId", product, "cantidad", 2)},
                {Map.of("productoId", product, "cantidad", 1), Map.of("productoId", product, "cantidad", 1)},
                {Map.of("productoId", product, "cantidad", Integer.MAX_VALUE), Map.of("productoId", product, "cantidad", 1)}}) {
            assertThat(request("POST", "/pedidos", Map.of("tipoEntrega", "RECOJO_TIENDA", "detalles", lines), owner).status()).isEqualTo(400);
            assertThat(request("GET", "/productos/" + product, null, owner).data().path("stock").asInt()).isEqualTo(10);
            assertThat(request("GET", "/pedidos/mios", null, owner).data()).isEmpty();
        }
    }
}
