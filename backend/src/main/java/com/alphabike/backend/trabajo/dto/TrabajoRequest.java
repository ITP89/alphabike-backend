package com.alphabike.backend.trabajo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrabajoRequest {

    @NotBlank(message = "El titulo es obligatorio")
    private String titulo;

    private String descripcion;
    private String citaId;
    private String imagenAntesUrl;
    private String imagenDespuesUrl;
    private Boolean destacado;
}