package com.alphabike.backend.proveedor;

import com.alphabike.backend.proveedor.dto.*;
import com.alphabike.backend.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/proveedores")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ProveedorController {

    private final ProveedorService proveedorService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProveedorResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok("Proveedores obtenidos", proveedorService.listar()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProveedorResponse>> obtener(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok("Proveedor obtenido", proveedorService.obtener(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProveedorResponse>> crear(
            @Valid @RequestBody ProveedorRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Proveedor creado", proveedorService.crear(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProveedorResponse>> actualizar(
            @PathVariable String id,
            @Valid @RequestBody ProveedorRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Proveedor actualizado", proveedorService.actualizar(id, request)));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<ProveedorResponse>> cambiarEstado(
            @PathVariable String id,
            @RequestParam String estado) {
        return ResponseEntity.ok(ApiResponse.ok("Estado de proveedor actualizado",
                proveedorService.cambiarEstado(id, estado)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> inactivar(@PathVariable String id) {
        proveedorService.inactivar(id);
        return ResponseEntity.ok(ApiResponse.ok("Proveedor inactivado"));
    }
}
