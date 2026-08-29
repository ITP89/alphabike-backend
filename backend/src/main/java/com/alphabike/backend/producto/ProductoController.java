package com.alphabike.backend.producto;

import com.alphabike.backend.producto.dto.*;
import com.alphabike.backend.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductoResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok("Productos obtenidos", productoService.listarActivos()));
    }

    @GetMapping("/todos")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO')")
    public ResponseEntity<ApiResponse<List<ProductoResponse>>> listarTodos() {
        return ResponseEntity.ok(ApiResponse.ok("Productos obtenidos", productoService.listar()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductoResponse>> obtener(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok("Producto obtenido", productoService.obtener(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO')")
    public ResponseEntity<ApiResponse<ProductoResponse>> crear(
            @Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Producto creado", productoService.crear(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO')")
    public ResponseEntity<ApiResponse<ProductoResponse>> actualizar(
            @PathVariable String id,
            @Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Producto actualizado", productoService.actualizar(id, request)));
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO')")
    public ResponseEntity<ApiResponse<ProductoResponse>> actualizarStock(
            @PathVariable String id,
            @RequestParam Integer stock) {
        return ResponseEntity.ok(ApiResponse.ok("Stock actualizado", productoService.actualizarStock(id, stock)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable String id) {
        productoService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.ok("Producto descontinuado"));
    }
}