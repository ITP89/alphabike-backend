package com.alphabike.backend.auth.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank
    @Email
    @Size(max = 255, message = "El campo no puede superar 255 caracteres")
    private String email;

    @NotBlank
    private String password;
}