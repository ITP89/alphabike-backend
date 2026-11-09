package com.alphabike.backend.usuario.dto;

import jakarta.validation.constraints.*;
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
    @Size(max = 255, message = "El campo no puede superar 255 caracteres")
    private String nombre;

    @Email(message = "El email no es valido")
    @Size(max = 255, message = "El campo no puede superar 255 caracteres")
    private String email;

    @NotBlank(message = "El telefono es obligatorio")
    @Size(max = 255, message = "El campo no puede superar 255 caracteres")
    private String telefono;

    private String passwordActual;

    @Size(min = 6, message = "La contrasena nueva debe tener al menos 6 caracteres")
    private String passwordNueva;
}
