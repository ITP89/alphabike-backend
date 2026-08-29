package com.alphabike.backend.config;

import com.alphabike.backend.usuario.Usuario;
import com.alphabike.backend.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AdminSeedConfig {

    @Value("${app.seed.admin.enabled:false}")
    private boolean adminSeedEnabled;

    @Value("${app.seed.admin.email:admin@alphabike.com}")
    private String adminEmail;

    @Value("${app.seed.admin.password:}")
    private String adminPassword;

    @Value("${app.seed.admin.name:Administrador AlphaBike}")
    private String adminName;

    @Value("${app.seed.admin.phone:999999999}")
    private String adminPhone;

    @Bean
    CommandLineRunner seedAdminUser(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!adminSeedEnabled) {
                return;
            }

            if (adminPassword == null || adminPassword.isBlank()) {
                throw new IllegalStateException("APP_SEED_ADMIN_PASSWORD es obligatorio cuando APP_SEED_ADMIN_ENABLED=true");
            }

            String email = adminEmail.trim().toLowerCase();
            Usuario admin = usuarioRepository.findByEmail(email)
                    .orElseGet(() -> Usuario.builder()
                            .email(email)
                            .build());

            admin.setNombre(adminName);
            admin.setTelefono(adminPhone);
            admin.setRol(Usuario.Rol.ADMIN);
            admin.setEstado(Usuario.Estado.ACTIVO);
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));

            usuarioRepository.save(admin);
        };
    }
}
