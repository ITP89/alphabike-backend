package com.alphabike.backend.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PerfilRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @Email(message = "El email no es valido")
    private String email;

    @NotBlank(message = "El telefono es obligatorio")
    private String telefono;

    private String passwordActual;

    @Size(min = 6, message = "La contrasena nueva debe tener al menos 6 caracteres")
    private String passwordNueva;
}
