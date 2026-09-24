package com.alphabike.backend.usuario;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios", schema = "auth_app")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Builder.Default
    @Column(name = "email_verificado", nullable = false)
    private boolean emailVerificado = false;

    @Column(name = "token_verificacion_email")
    private String tokenVerificacionEmail;

    @Column(name = "fecha_expiracion_verificacion")
    private LocalDateTime fechaExpiracionVerificacion;

    @Column(name = "token_recuperacion_password")
    private String tokenRecuperacionPassword;

    @Column(name = "fecha_expiracion_password")
    private LocalDateTime fechaExpiracionPassword;

    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
    }

    public enum Rol {
        ADMIN, ENCARGADO, CLIENTE
    }

    public enum Estado {
        ACTIVO, INACTIVO
    }
}