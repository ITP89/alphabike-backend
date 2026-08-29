package com.alphabike.backend.cotizacion;

import com.alphabike.backend.cotizacion.dto.*;
import com.alphabike.backend.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cotizaciones")
@RequiredArgsConstructor
public class CotizacionController {

    private final CotizacionService cotizacionService;

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CotizacionResponse>> obtener(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok("Cotizacion obtenida", cotizacionService.obtener(id)));
    }

    @GetMapping("/cita/{citaId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CotizacionResponse>> obtenerPorCita(@PathVariable String citaId) {
        return ResponseEntity.ok(ApiResponse.ok("Cotizacion obtenida", cotizacionService.obtenerPorCita(citaId)));
    }

    @PostMapping("/cita/{citaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO')")
    public ResponseEntity<ApiResponse<CotizacionResponse>> crearOActualizar(
            @PathVariable String citaId,
            @Valid @RequestBody CotizacionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Cotizacion guardada",
                cotizacionService.crearOActualizar(citaId, request)));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CotizacionResponse>> cambiarEstado(
            @PathVariable String id,
            @RequestParam String estado,
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado",
                cotizacionService.cambiarEstado(id, estado, email)));
    }
}