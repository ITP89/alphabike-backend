package com.alphabike.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResendVerificationRequest {

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Ingresa un correo electrónico válido")
    private String email;
}
