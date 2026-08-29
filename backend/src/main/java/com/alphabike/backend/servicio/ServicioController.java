package com.alphabike.backend.servicio;

import com.alphabike.backend.servicio.dto.*;
import com.alphabike.backend.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/servicios")
@RequiredArgsConstructor
public class ServicioController {

    private final ServicioService servicioService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ServicioResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok("Servicios obtenidos", servicioService.listar()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServicioResponse>> obtener(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok("Servicio obtenido", servicioService.obtener(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ServicioResponse>> crear(
            @Valid @RequestBody ServicioRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Servicio creado", servicioService.crear(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ServicioResponse>> actualizar(
            @PathVariable String id,
            @Valid @RequestBody ServicioRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Servicio actualizado", servicioService.actualizar(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable String id) {
        servicioService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.ok("Servicio eliminado"));
    }
}