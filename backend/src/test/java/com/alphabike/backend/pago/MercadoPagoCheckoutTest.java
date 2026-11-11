package com.alphabike.backend.pago;

import com.alphabike.backend.pedido.Pedido;
import com.alphabike.backend.pedido.PedidoRepository;
import com.alphabike.backend.pedido.PedidoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class MercadoPagoCheckoutTest {
    private static final String ORDER_ID = "ORDTST01KS5AJ6HTK2HRQ3XJ3C2JCKP9";
    private final PedidoRepository pedidos = mock(PedidoRepository.class);
    private final PagoRepository pagos = mock(PagoRepository.class);
    private final PedidoService pedidoService = mock(PedidoService.class);
    private final MercadoPagoCheckout checkout = new MercadoPagoCheckout(pedidos, pagos, pedidoService, RestClient.builder());
    private final Pedido pedido = Pedido.builder().id("pedido-1").mercadoPagoOrderId(ORDER_ID)
            .estado(Pedido.Estado.PENDIENTE).total(new BigDecimal("50.00")).build();

    MercadoPagoCheckoutTest() {
        ReflectionTestUtils.setField(checkout, "sellerId", "12345");
    }

    @Test
    void pagoAcreditadoSeRegistraUnaVez() {
        ObjectNode remoto = respuesta("processed", "accredited", "50.00");
        checkout.aplicar(pedido, remoto);
        checkout.aplicar(pedido, remoto);
        assertEquals(Pedido.Estado.PAGADO, pedido.getEstado());
        verify(pagos).save(argThat(pago -> ORDER_ID.equals(pago.getTransaccionId())
                && pago.getMetodoPago() == Pago.MetodoPago.MERCADO_PAGO));
        verify(pedidos).save(pedido);
    }

    @Test
    void respuestaDeOtroPedidoVendedorOMonedaSeRechaza() {
        for (String campo : new String[]{"external_reference", "user_id", "currency", "total_amount", "id"}) {
            ObjectNode remoto = respuesta("processed", "accredited", "50.00");
            remoto.put(campo, "incorrecto");
            assertThrows(ResponseStatusException.class, () -> checkout.aplicar(pedido, remoto), campo);
        }
        ReflectionTestUtils.setField(checkout, "live", true);
        assertThrows(ResponseStatusException.class,
                () -> checkout.aplicar(pedido, respuesta("processed", "accredited", "50.00")));
        verifyNoInteractions(pagos, pedidos);
    }

    @Test
    void pendienteOImporteParcialNoMarcaPagado() {
        checkout.aplicar(pedido, respuesta("processing", "in_process", "0"));
        checkout.aplicar(pedido, respuesta("processed", "accredited", "49.99"));
        assertEquals(Pedido.Estado.PENDIENTE, pedido.getEstado());
        verifyNoInteractions(pagos, pedidos, pedidoService);
    }

    @Test
    void cancelacionSinCobroLiberaLaReserva() {
        checkout.aplicar(pedido, respuesta("canceled", "canceled", "1.00"));
        verifyNoInteractions(pedidoService);
        checkout.aplicar(pedido, respuesta("canceled", "canceled", "0.00"));
        verify(pedidoService).cancelarMercadoPagoConfirmado("pedido-1", ORDER_ID);
        verifyNoInteractions(pagos);
    }

    @Test
    void firmaDeOtroPedidoYRedireccionExternaSeRechazan() {
        String firma = "ts=1742505638683,v1=e980b34173180138df9b695e0a68b30549c1b52d1f007e7613b17ab9138c7f5b";
        assertTrue(MercadoPagoCheckout.firmaValida(ORDER_ID, firma, "request-1", "secret-de-prueba"));
        assertFalse(MercadoPagoCheckout.firmaValida(ORDER_ID + "A", firma, "request-1", "secret-de-prueba"));
        assertThrows(ResponseStatusException.class,
                () -> MercadoPagoCheckout.validarUrl("https://www.mercadopago.com.pe.ejemplo.test/checkout/v1"));
        String url = "https://www.mercadopago.com.pe/checkout/v1/redirect?order_id=" + ORDER_ID;
        assertEquals(url, MercadoPagoCheckout.validarUrl(url));
    }

    private ObjectNode respuesta(String estado, String detalle, String pagado) {
        return new ObjectMapper().createObjectNode()
                .put("id", ORDER_ID).put("external_reference", "pedido-1")
                .put("user_id", "12345").put("currency", "PEN").put("total_amount", "50.00")
                .put("status", estado).put("status_detail", detalle).put("total_paid_amount", pagado);
    }
}
