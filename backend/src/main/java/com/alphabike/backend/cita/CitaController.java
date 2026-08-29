package com.alphabike.backend.cita;

import com.alphabike.backend.cita.dto.*;
import com.alphabike.backend.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/citas")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO')")
    public ResponseEntity<ApiResponse<List<CitaResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok("Citas obtenidas", citaService.listar()));
    }

    @GetMapping("/mias")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<ApiResponse<List<CitaResponse>>> listarMias(
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(ApiResponse.ok("Citas obtenidas", citaService.listarPorCliente(email)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CitaResponse>> obtener(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok("Cita obtenida", citaService.obtener(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<ApiResponse<CitaResponse>> crear(
            @Valid @RequestBody CitaRequest request,
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(ApiResponse.ok("Cita creada", citaService.crear(request, email)));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO')")
    public ResponseEntity<ApiResponse<CitaResponse>> cambiarEstado(
            @PathVariable String id,
            @RequestParam String estado) {
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado", citaService.cambiarEstado(id, estado)));
    }

    @PatchMapping("/{id}/asignar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CitaResponse>> asignarEncargado(
            @PathVariable String id,
            @RequestParam String encargadoId) {
        return ResponseEntity.ok(ApiResponse.ok("Encargado asignado", citaService.asignarEncargado(id, encargadoId)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> cancelar(
            @PathVariable String id,
            @AuthenticationPrincipal String email) {
        citaService.cancelar(id, email);
        return ResponseEntity.ok(ApiResponse.ok("Cita cancelada"));
    }
}