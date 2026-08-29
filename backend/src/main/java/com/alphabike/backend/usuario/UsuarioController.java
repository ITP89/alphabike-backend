package com.alphabike.backend.usuario;

import com.alphabike.backend.shared.response.ApiResponse;
import com.alphabike.backend.usuario.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UsuarioResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok("Usuarios obtenidos", usuarioService.listar()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO')")
    public ResponseEntity<ApiResponse<UsuarioResponse>> obtener(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok("Usuario obtenido", usuarioService.obtener(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UsuarioResponse>> crear(
            @Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Usuario creado", usuarioService.crear(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UsuarioResponse>> actualizar(
            @PathVariable String id,
            @Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Usuario actualizado", usuarioService.actualizar(id, request)));
    }

    @PutMapping("/perfil")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UsuarioResponse>> actualizarPerfilPropio(
            @Valid @RequestBody PerfilRequest request,
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(ApiResponse.ok("Perfil actualizado",
                usuarioService.actualizarPerfilPropio(email, request)));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UsuarioResponse>> cambiarEstado(
            @PathVariable String id,
            @RequestParam String estado) {
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado",
                usuarioService.cambiarEstado(id, estado)));
    }

    @PatchMapping("/{id}/rol")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UsuarioResponse>> cambiarRol(
            @PathVariable String id,
            @RequestParam String rol) {
        return ResponseEntity.ok(ApiResponse.ok("Rol actualizado",
                usuarioService.cambiarRol(id, rol)));
    }

    @GetMapping("/{id}/historial")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO')")
    public ResponseEntity<ApiResponse<HistorialClienteResponse>> historial(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok("Historial obtenido", usuarioService.historial(id)));
    }
}
