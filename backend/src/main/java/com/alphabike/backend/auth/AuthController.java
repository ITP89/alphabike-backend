package com.alphabike.backend.auth;

import com.alphabike.backend.auth.dto.*;
import com.alphabike.backend.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.ok("Registro exitoso. Te hemos enviado un correo de activación.", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login exitoso", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthResponse>> me(
            @AuthenticationPrincipal String email) {
        AuthResponse response = authService.me(email);
        return ResponseEntity.ok(ApiResponse.ok("Usuario autenticado", response));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<String>> verifyEmail(
            @RequestParam("token") String token) {
        String mensaje = authService.verificarEmail(token);
        return ResponseEntity.ok(ApiResponse.ok(mensaje, mensaje));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        String mensaje = authService.solicitarRecuperacionPassword(request);
        return ResponseEntity.ok(ApiResponse.ok(mensaje, mensaje));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        String mensaje = authService.restablecerPassword(request);
        return ResponseEntity.ok(ApiResponse.ok(mensaje, mensaje));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<String>> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request) {
        String mensaje = authService.reenviarVerificacion(request);
        return ResponseEntity.ok(ApiResponse.ok(mensaje, mensaje));
    }
}