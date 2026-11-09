package com.alphabike.backend.auth.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForgotPasswordRequest {

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Ingresa un correo electrónico válido")
    @Size(max = 255, message = "El campo no puede superar 255 caracteres")
    private String email;
}
