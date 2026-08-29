package com.alphabike.backend.usuario.dto;

import com.alphabike.backend.usuario.Usuario;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponse {

    private String id;
    private String nombre;
    private String email;
    private String telefono;
    private String rol;
    private String estado;
    private LocalDateTime fechaRegistro;

    public static UsuarioResponse from(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .telefono(usuario.getTelefono())
                .rol(usuario.getRol().name())
                .estado(usuario.getEstado().name())
                .fechaRegistro(usuario.getFechaRegistro())
                .build();
    }
}