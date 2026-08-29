package com.alphabike.backend.auth.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String id;
    private String token;
    private String nombre;
    private String email;
    private String telefono;
    private String rol;
}
