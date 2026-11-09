package com.alphabike.backend.categoria.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El campo no puede superar 255 caracteres")
    private String nombre;

    @Size(max = 255, message = "El campo no puede superar 255 caracteres")
    private String descripcion;
}