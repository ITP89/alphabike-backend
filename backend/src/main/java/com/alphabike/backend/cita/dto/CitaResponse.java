package com.alphabike.backend.cita.dto;

import com.alphabike.backend.cita.Cita;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CitaResponse {

    private String id;
    private String clienteId;
    private String clienteNombre;
    private String encargadoId;
    private String encargadoNombre;
    private String servicioId;
    private String servicioNombre;
    private LocalDate fecha;
    private LocalTime hora;
    private String estado;
    private String biciDescripcion;
    private String observaciones;

    public static CitaResponse from(Cita cita) {
        return CitaResponse.builder()
                .id(cita.getId())
                .clienteId(cita.getCliente().getId())
                .clienteNombre(cita.getCliente().getNombre())
                .encargadoId(cita.getEncargado() != null ? cita.getEncargado().getId() : null)
                .encargadoNombre(cita.getEncargado() != null ? cita.getEncargado().getNombre() : null)
                .servicioId(cita.getServicio().getId())
                .servicioNombre(cita.getServicio().getNombre())
                .fecha(cita.getFecha())
                .hora(cita.getHora())
                .estado(cita.getEstado().name())
                .biciDescripcion(cita.getBiciDescripcion())
                .observaciones(cita.getObservaciones())
                .build();
    }
}