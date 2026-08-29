package com.alphabike.backend.pago;

import com.alphabike.backend.pago.dto.*;
import com.alphabike.backend.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO')")
    public ResponseEntity<ApiResponse<List<PagoResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok("Pagos obtenidos", pagoService.listar()));
    }

    @GetMapping("/pendientes")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO')")
    public ResponseEntity<ApiResponse<List<PagoResponse>>> listarPendientes() {
        return ResponseEntity.ok(ApiResponse.ok("Pagos pendientes obtenidos", pagoService.listarPendientes()));
    }

    @GetMapping("/referencia/{referenciaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO')")
    public ResponseEntity<ApiResponse<List<PagoResponse>>> listarPorReferencia(
            @PathVariable String referenciaId) {
        return ResponseEntity.ok(ApiResponse.ok("Pagos obtenidos",
                pagoService.listarPorReferencia(referenciaId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO')")
    public ResponseEntity<ApiResponse<PagoResponse>> registrar(
            @Valid @RequestBody PagoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Pago registrado", pagoService.registrar(request)));
    }
}
