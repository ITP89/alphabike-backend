package com.alphabike.backend.pago;

import com.alphabike.backend.shared.response.ApiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mercado-pago")
@RequiredArgsConstructor
public class MercadoPagoController {
    private final MercadoPagoCheckout checkout;

    @GetMapping("/config")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public ApiResponse<MercadoPagoCheckout.Config> config() {
        return ApiResponse.ok("Configuracion de pago", checkout.config());
    }

    @PostMapping("/pedidos/{id}/inicio")
    @PreAuthorize("hasRole('CLIENTE')")
    public ApiResponse<MercadoPagoCheckout.Inicio> iniciar(@PathVariable String id, @AuthenticationPrincipal String email) {
        return ApiResponse.ok("Checkout preparado", checkout.iniciar(id, email));
    }

    @PostMapping("/pedidos/{id}/sincronizar")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<Void> sincronizar(@PathVariable String id, @AuthenticationPrincipal String email) {
        checkout.sincronizar(id, email);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestParam(name = "data.id", required = false) String id,
                                         @RequestHeader(name = "x-signature", required = false) String firma,
                                         @RequestHeader(name = "x-request-id", required = false) String requestId,
                                         @RequestBody JsonNode body) {
        checkout.notificacion(id, firma, requestId, body);
        return ResponseEntity.ok().build();
    }
}
