package com.alphabike.backend.trabajo.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrabajoRequest {

    @NotBlank(message = "El titulo es obligatorio")
    @Size(max = 255, message = "El campo no puede superar 255 caracteres")
    private String titulo;

    @Size(max = 255, message = "El campo no puede superar 255 caracteres")
    private String descripcion;
    @Size(max = 255, message = "El campo no puede superar 255 caracteres")
    private String citaId;
    @Size(max = 255, message = "La URL no puede superar 255 caracteres")
    private String imagenAntesUrl;
    @Size(max = 255, message = "La URL no puede superar 255 caracteres")
    private String imagenDespuesUrl;
    private Boolean destacado;
}