package com.alphabike.backend.pedido;

import com.alphabike.backend.pedido.dto.*;
import com.alphabike.backend.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO')")
    public ResponseEntity<ApiResponse<List<PedidoResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok("Pedidos obtenidos", pedidoService.listar()));
    }

    @GetMapping("/mios")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<ApiResponse<List<PedidoResponse>>> listarMios(
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(ApiResponse.ok("Pedidos obtenidos", pedidoService.listarPorCliente(email)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PedidoResponse>> obtener(
            @PathVariable String id,
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(ApiResponse.ok("Pedido obtenido", pedidoService.obtener(id, email)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'ENCARGADO')")
    public ResponseEntity<ApiResponse<PedidoResponse>> crear(
            @Valid @RequestBody PedidoRequest request,
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(ApiResponse.ok("Pedido creado", pedidoService.crear(request, email)));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO')")
    public ResponseEntity<ApiResponse<PedidoResponse>> cambiarEstado(
            @PathVariable String id,
            @RequestParam String estado) {
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado", pedidoService.cambiarEstado(id, estado)));
    }

    @PatchMapping("/{id}/seguimiento")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO')")
    public ResponseEntity<ApiResponse<PedidoResponse>> registrarSeguimiento(
            @PathVariable String id,
            @RequestParam String numeroSeguimiento) {
        return ResponseEntity.ok(ApiResponse.ok("Seguimiento registrado",
                pedidoService.registrarSeguimiento(id, numeroSeguimiento)));
    }

    @PatchMapping("/{id}/detalle/{detalleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO')")
    public ResponseEntity<ApiResponse<PedidoResponse>> actualizarPrecioAcordado(
            @PathVariable String id,
            @PathVariable String detalleId,
            @RequestParam BigDecimal precioAcordado) {
        return ResponseEntity.ok(ApiResponse.ok("Precio actualizado",
                pedidoService.actualizarPrecioAcordado(id, detalleId, precioAcordado)));
    }
}