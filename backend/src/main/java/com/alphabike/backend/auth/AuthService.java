package com.alphabike.backend.auth;

import com.alphabike.backend.auth.dto.*;
import com.alphabike.backend.email.EmailService;
import com.alphabike.backend.security.JwtTokenProvider;
import com.alphabike.backend.shared.exception.BadRequestException;
import com.alphabike.backend.shared.exception.ResourceNotFoundException;
import com.alphabike.backend.usuario.Usuario;
import com.alphabike.backend.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());

        if (usuarioRepository.existsByEmail(email)) {
            throw new BadRequestException("El email ya está registrado");
        }

        String tokenVerificacion = UUID.randomUUID().toString();

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .telefono(request.getTelefono())
                .rol(Usuario.Rol.CLIENTE)
                .estado(Usuario.Estado.ACTIVO)
                .emailVerificado(false)
                .tokenVerificacionEmail(tokenVerificacion)
                .fechaExpiracionVerificacion(LocalDateTime.now().plusHours(24))
                .build();

        usuarioRepository.save(usuario);

        // Enviar correo de activación
        try {
            emailService.enviarVerificacionEmail(usuario.getEmail(), usuario.getNombre(), tokenVerificacion);
        } catch (Exception e) {
            log.error("Error al procesar envío de correo de activación para {}: {}", email, e.getMessage());
        }

        return toAuthResponse(usuario, false);
    }

    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(normalizeEmail(request.getEmail()))
                .orElseThrow(() -> new ResourceNotFoundException("Credenciales incorrectas"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            throw new BadRequestException("Credenciales incorrectas");
        }

        if (usuario.getEstado() == Usuario.Estado.INACTIVO) {
            throw new BadRequestException("Usuario inactivo");
        }

        if (!usuario.isEmailVerificado()) {
            throw new BadRequestException("Tu cuenta aún no ha sido activada. Por favor confirma tu correo electrónico antes de ingresar.");
        }

        return toAuthResponse(usuario, true);
    }

    public AuthResponse me(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return toAuthResponse(usuario, true);
    }

    public String verificarEmail(String token) {
        if (token == null || token.isBlank()) {
            throw new BadRequestException("Token de verificación no proporcionado");
        }

        Usuario usuario = usuarioRepository.findByTokenVerificacionEmail(token)
                .orElseThrow(() -> new ResourceNotFoundException("El enlace de verificación no es válido o ya fue utilizado"));

        if (usuario.getFechaExpiracionVerificacion() != null && LocalDateTime.now().isAfter(usuario.getFechaExpiracionVerificacion())) {
            throw new BadRequestException("El enlace de activación ha expirado. Por favor solicita un nuevo correo de confirmación.");
        }

        usuario.setEmailVerificado(true);
        usuario.setTokenVerificacionEmail(null);
        usuario.setFechaExpiracionVerificacion(null);
        usuarioRepository.save(usuario);

        return "Tu cuenta ha sido activada exitosamente. Ya puedes iniciar sesión en AlphaBike.";
    }

    public String solicitarRecuperacionPassword(ForgotPasswordRequest request) {
        String email = normalizeEmail(request.getEmail());

        usuarioRepository.findByEmail(email).ifPresent(usuario -> {
            String token = UUID.randomUUID().toString();
            usuario.setTokenRecuperacionPassword(token);
            usuario.setFechaExpiracionPassword(LocalDateTime.now().plusMinutes(30));
            usuarioRepository.save(usuario);

            try {
                emailService.enviarRecuperacionPassword(usuario.getEmail(), usuario.getNombre(), token);
            } catch (Exception e) {
                log.error("Error al procesar envío de correo de recuperación para {}: {}", email, e.getMessage());
            }
        });

        // Respuesta genérica por seguridad para evitar enumeración de correos
        return "Si el correo está registrado en AlphaBike, recibirás un enlace para restablecer tu contraseña.";
    }

    public String restablecerPassword(ResetPasswordRequest request) {
        if (request.getToken() == null || request.getToken().isBlank()) {
            throw new BadRequestException("Token de recuperación no válido");
        }

        Usuario usuario = usuarioRepository.findByTokenRecuperacionPassword(request.getToken())
                .orElseThrow(() -> new ResourceNotFoundException("El enlace de restablecimiento no es válido o ya ha sido utilizado."));

        if (usuario.getFechaExpiracionPassword() != null && LocalDateTime.now().isAfter(usuario.getFechaExpiracionPassword())) {
            throw new BadRequestException("El enlace de restablecimiento ha expirado. Solicita uno nuevo.");
        }

        usuario.setPasswordHash(passwordEncoder.encode(request.getNuevaPassword()));
        usuario.setTokenRecuperacionPassword(null);
        usuario.setFechaExpiracionPassword(null);
        // Si no estaba verificado, restablecer con acceso al correo confirma implícitamente su identidad
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);

        return "Contraseña actualizada exitosamente. Ya puedes iniciar sesión con tu nueva contraseña.";
    }

    public String reenviarVerificacion(ResendVerificationRequest request) {
        String email = normalizeEmail(request.getEmail());

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró ningún usuario con ese correo"));

        if (usuario.isEmailVerificado()) {
            return "Tu correo electrónico ya se encuentra verificado.";
        }

        String nuevoToken = UUID.randomUUID().toString();
        usuario.setTokenVerificacionEmail(nuevoToken);
        usuario.setFechaExpiracionVerificacion(LocalDateTime.now().plusHours(24));
        usuarioRepository.save(usuario);

        emailService.enviarVerificacionEmail(usuario.getEmail(), usuario.getNombre(), nuevoToken);

        return "Se ha reenviado un nuevo correo de activación a tu bandeja de entrada.";
    }

    private AuthResponse toAuthResponse(Usuario usuario, boolean incluirToken) {
        String token = incluirToken
                ? jwtTokenProvider.generateToken(usuario.getEmail(), usuario.getRol().name())
                : null;

        return AuthResponse.builder()
                .id(usuario.getId())
                .token(token)
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .telefono(usuario.getTelefono())
                .rol(usuario.getRol().name())
                .emailVerificado(usuario.isEmailVerificado())
                .build();
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
