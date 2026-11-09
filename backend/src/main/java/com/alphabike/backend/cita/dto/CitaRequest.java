package com.alphabike.backend.cita.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CitaRequest {

    @NotBlank(message = "El servicio es obligatorio")
    private String servicioId;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria")
    private LocalTime hora;

    @NotBlank(message = "La descripcion de la bicicleta es obligatoria")
    @Size(min = 4, max = 255, message = "La descripcion de la bicicleta debe tener entre 4 y 255 caracteres")
    private String biciDescripcion;
    @Size(max = 255, message = "Las observaciones no pueden superar 255 caracteres")
    private String observaciones;
}