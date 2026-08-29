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

            // Seed ENCARGADO
            String encargadoEmail = "encargado@alphabike.com";
            Usuario encargado = usuarioRepository.findByEmail(encargadoEmail)
                    .orElseGet(() -> Usuario.builder().email(encargadoEmail).build());
            encargado.setNombre("Encargado Taller AlphaBike");
            encargado.setTelefono("987654321");
            encargado.setRol(Usuario.Rol.ENCARGADO);
            encargado.setEstado(Usuario.Estado.ACTIVO);
            encargado.setPasswordHash(passwordEncoder.encode("encargado1234"));
            usuarioRepository.save(encargado);

            // Seed CLIENTE
            String clienteEmail = "cliente@alphabike.com";
            Usuario cliente = usuarioRepository.findByEmail(clienteEmail)
                    .orElseGet(() -> Usuario.builder().email(clienteEmail).build());
            cliente.setNombre("Cliente Frecuente AlphaBike");
            cliente.setTelefono("912345678");
            cliente.setRol(Usuario.Rol.CLIENTE);
            cliente.setEstado(Usuario.Estado.ACTIVO);
            cliente.setPasswordHash(passwordEncoder.encode("cliente1234"));
            usuarioRepository.save(cliente);
        };
    }
}
