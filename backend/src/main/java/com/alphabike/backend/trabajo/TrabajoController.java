package com.alphabike.backend.trabajo;

import com.alphabike.backend.shared.response.ApiResponse;
import com.alphabike.backend.trabajo.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trabajos")
@RequiredArgsConstructor
public class TrabajoController {

    private final TrabajoService trabajoService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TrabajoResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok("Trabajos obtenidos", trabajoService.listar()));
    }

    @GetMapping("/destacados")
    public ResponseEntity<ApiResponse<List<TrabajoResponse>>> listarDestacados() {
        return ResponseEntity.ok(ApiResponse.ok("Trabajos destacados obtenidos",
                trabajoService.listarDestacados()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TrabajoResponse>> obtener(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok("Trabajo obtenido", trabajoService.obtener(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO')")
    public ResponseEntity<ApiResponse<TrabajoResponse>> crear(
            @Valid @RequestBody TrabajoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Trabajo publicado", trabajoService.crear(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO')")
    public ResponseEntity<ApiResponse<TrabajoResponse>> actualizar(
            @PathVariable String id,
            @Valid @RequestBody TrabajoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Trabajo actualizado",
                trabajoService.actualizar(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable String id) {
        trabajoService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.ok("Trabajo eliminado"));
    }
}