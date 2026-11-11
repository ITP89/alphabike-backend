package com.alphabike.backend.pago;

import com.alphabike.backend.pedido.Pedido;
import com.alphabike.backend.pedido.PedidoRepository;
import com.alphabike.backend.pedido.PedidoService;
import com.alphabike.backend.shared.exception.BadRequestException;
import com.alphabike.backend.shared.exception.ResourceNotFoundException;
import com.alphabike.backend.shared.exception.UnauthorizedException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class MercadoPagoCheckout {
    private final PedidoRepository pedidos;
    private final PagoRepository pagos;
    private final PedidoService pedidoService;
    private final RestClient provider;

    @Value("${app.payment.mercado-pago.enabled:false}") private boolean enabled;
    @Value("${app.payment.mercado-pago.live:false}") private boolean live;
    @Value("${app.payment.mercado-pago.token:}") private String token;
    @Value("${app.payment.mercado-pago.webhook-secret:}") private String webhookSecret;
    @Value("${app.payment.mercado-pago.seller-id:}") private String sellerId;
    @Value("${app.frontend.url:http://localhost:5173}") private String frontendUrl;

    public MercadoPagoCheckout(PedidoRepository pedidos, PagoRepository pagos,
                              PedidoService pedidoService, RestClient.Builder builder) {
        this.pedidos = pedidos;
        this.pagos = pagos;
        this.pedidoService = pedidoService;
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(4)).build());
        factory.setReadTimeout(Duration.ofSeconds(10));
        this.provider = builder.baseUrl("https://api.mercadopago.com").requestFactory(factory).build();
    }

    public record Config(boolean enabled, boolean ready, boolean live) {}
    public record Inicio(String checkoutUrl) {}

    public Config config() {
        return new Config(enabled, enabled && !token.isBlank() && !webhookSecret.isBlank()
                && sellerId.matches("[0-9]+") && (!live || !token.startsWith("TEST-")), live);
    }

    @Transactional
    public Inicio iniciar(String id, String email) {
        exigirConfiguracion();
        Pedido pedido = pedidoPropio(id, email);
        if (pedido.getEstado() != Pedido.Estado.PENDIENTE) {
            throw new BadRequestException("El pedido no esta pendiente de pago");
        }
        if (pedido.getMercadoPagoOrderId() != null) {
            JsonNode actual = consultar(pedido.getMercadoPagoOrderId());
            validarPedidoRemoto(pedido, actual);
            if (!"created".equals(actual.path("status").asText())
                    && !"action_required".equals(actual.path("status").asText())) {
                throw new BadRequestException("Consulta el estado del pago antes de volver a intentarlo");
            }
            return new Inicio(validarUrl(pedido.getMercadoPagoCheckoutUrl()));
        }
        String total = pedido.getTotal().toPlainString();
        String retorno = frontendUrl.replaceFirst("/+$", "") + "/pedidos/" + pedido.getId();
        JsonNode remoto = llamada(() -> provider.post().uri("/v1/orders")
                .headers(h -> h.setBearerAuth(token))
                .header("X-Idempotency-Key", pedido.getId())
                .body(Map.of("type", "online", "processing_mode", "manual",
                        "total_amount", total, "external_reference", pedido.getId(),
                        "expiration_time", "PT30M",
                        "payer", Map.of("email", pedido.getCliente().getEmail()),
                        "items", List.of(Map.of("title", "Pedido AlphaBike", "quantity", 1, "unit_price", total)),
                        "config", Map.of("online", Map.of("success_url", retorno,
                                "pending_url", retorno, "failure_url", retorno))))
                .retrieve().body(JsonNode.class));
        validarPedidoRemoto(pedido, remoto);
        String url = validarUrl(remoto.path("checkout_url").asText());
        pedido.setMercadoPagoOrderId(remoto.path("id").asText());
        pedido.setMercadoPagoCheckoutUrl(url);
        pedidos.save(pedido);
        return new Inicio(url);
    }

    @Transactional
    public void sincronizar(String id, String email) {
        exigirConfiguracion();
        Pedido pedido = pedidoPropio(id, email);
        if (pedido.getMercadoPagoOrderId() != null) {
            aplicar(pedido, consultar(pedido.getMercadoPagoOrderId()));
        }
    }

    @Transactional
    public void notificacion(String id, String firma, String requestId, JsonNode body) {
        exigirConfiguracion();
        if (!idValido(id) || !firmaValida(id, firma, requestId, webhookSecret)
                || !body.path("live_mode").isBoolean() || body.path("live_mode").asBoolean() != live
                || !"order".equals(body.path("type").asText())
                || !id.equals(body.path("data").path("id").asText())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Notificacion no valida");
        }
        var asociado = pedidos.findIdByMercadoPagoOrderId(id);
        if (asociado.isEmpty()) return;
        Pedido pedido = pedidos.findByIdForUpdate(asociado.get()).orElseThrow();
        aplicar(pedido, consultar(id));
    }

    void aplicar(Pedido pedido, JsonNode remoto) {
        validarPedidoRemoto(pedido, remoto);
        if (pedido.getEstado() != Pedido.Estado.PENDIENTE) return;
        String estado = remoto.path("status").asText();
        if ("canceled".equals(estado) || "expired".equals(estado)) {
            if (montoIgual(remoto.path("total_paid_amount"), BigDecimal.ZERO)) {
                pedidoService.cancelarMercadoPagoConfirmado(pedido.getId(), pedido.getMercadoPagoOrderId());
            }
            return;
        }
        if (!"processed".equals(estado) || !"accredited".equals(remoto.path("status_detail").asText())
                || !montoIgual(remoto.path("total_paid_amount"), pedido.getTotal())) return;

        if (pagos.existsByReferenciaTipoAndReferenciaIdAndEstado(
                Pago.ReferenciaTipo.PEDIDO, pedido.getId(), Pago.Estado.PAGADO)) return;
        pagos.save(Pago.builder().referenciaTipo(Pago.ReferenciaTipo.PEDIDO)
                .referenciaId(pedido.getId()).monto(pedido.getTotal())
                .metodoPago(Pago.MetodoPago.MERCADO_PAGO).estado(Pago.Estado.PAGADO)
                .transaccionId(pedido.getMercadoPagoOrderId()).build());
        pedido.setEstado(Pedido.Estado.PAGADO);
        pedidos.save(pedido);
    }

    private Pedido pedidoPropio(String id, String email) {
        Pedido pedido = pedidos.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
        if (!pedido.getCliente().getEmail().equalsIgnoreCase(email)) {
            throw new UnauthorizedException("No tienes permiso para pagar este pedido");
        }
        return pedido;
    }

    private void exigirConfiguracion() {
        if (!config().ready()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Mercado Pago aun no esta habilitado");
    }

    private JsonNode consultar(String id) {
        if (!idValido(id)) throw proveedorInvalido();
        return llamada(() -> provider.get().uri("/v1/orders/{id}", id)
                .headers(h -> h.setBearerAuth(token)).retrieve().body(JsonNode.class));
    }

    private JsonNode llamada(java.util.function.Supplier<JsonNode> accion) {
        try {
            JsonNode resultado = accion.get();
            if (resultado == null || !resultado.isObject()) throw proveedorInvalido();
            return resultado;
        } catch (RestClientException e) {
            // Provider response may contain private buyer data: do not return or log its body.
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Mercado Pago no respondio correctamente");
        }
    }

    private void validarPedidoRemoto(Pedido pedido, JsonNode remoto) {
        String id = remoto.path("id").asText();
        if (!idValido(id) || (pedido.getMercadoPagoOrderId() != null && !pedido.getMercadoPagoOrderId().equals(id))
                || id.startsWith("ORDTST") == live
                || !pedido.getId().equals(remoto.path("external_reference").asText())
                || !sellerId.equals(remoto.path("user_id").asText())
                || !"PEN".equals(remoto.path("currency").asText())
                || !montoIgual(remoto.path("total_amount"), pedido.getTotal())) throw proveedorInvalido();
    }

    static boolean montoIgual(JsonNode valor, BigDecimal esperado) {
        try { return new BigDecimal(valor.asText()).compareTo(esperado) == 0; }
        catch (NumberFormatException e) { return false; }
    }

    static String validarUrl(String url) {
        try {
            URI parsed = URI.create(url);
            String host = parsed.getHost();
            if (url.length() <= 2048 && "https".equalsIgnoreCase(parsed.getScheme())
                    && ("www.mercadopago.com.pe".equalsIgnoreCase(host) || "mercadopago.com.pe".equalsIgnoreCase(host))
                    && parsed.getUserInfo() == null && (parsed.getPort() == -1 || parsed.getPort() == 443)
                    && parsed.getPath().startsWith("/checkout/")) return url;
        } catch (IllegalArgumentException ignored) { }
        throw proveedorInvalido();
    }

    static boolean firmaValida(String id, String firma, String requestId, String secret) {
        if (firma == null || requestId == null || !requestId.matches("[a-zA-Z0-9-]{1,128}")
                || firma.length() > 256 || secret == null || secret.isBlank()) return false;
        String ts = null, v1 = null;
        for (String elemento : firma.split(",")) {
            String[] parte = elemento.trim().split("=", 2);
            if (parte.length != 2) return false;
            if (parte[0].equals("ts")) { if (ts != null) return false; ts = parte[1]; }
            if (parte[0].equals("v1")) { if (v1 != null) return false; v1 = parte[1]; }
        }
        if (ts == null || !ts.matches("[0-9]{1,16}") || v1 == null || !v1.matches("[0-9a-fA-F]{64}")) return false;
        try {
            var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String manifest = "id:" + id.toLowerCase(Locale.ROOT) + ";request-id:" + requestId + ";ts:" + ts + ";";
            return MessageDigest.isEqual(mac.doFinal(manifest.getBytes(StandardCharsets.UTF_8)), HexFormat.of().parseHex(v1));
        } catch (java.security.GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo validar la firma", e);
        }
    }

    private static boolean idValido(String id) {
        return id != null && id.matches("ORD[A-Z0-9]{5,60}");
    }

    private static ResponseStatusException proveedorInvalido() {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, "La operacion de Mercado Pago no coincide con el pedido");
    }
}
