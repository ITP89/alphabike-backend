package com.alphabike.backend.auth.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank
    @Size(max = 255, message = "El campo no puede superar 255 caracteres")
    private String nombre;

    @NotBlank
    @Email
    @Size(max = 255, message = "El campo no puede superar 255 caracteres")
    private String email;

    @NotBlank
    @Size(min = 6, message = "La contrasena debe tener al menos 6 caracteres")
    private String password;

    @NotBlank
    @Size(max = 255, message = "El campo no puede superar 255 caracteres")
    private String telefono;
}
