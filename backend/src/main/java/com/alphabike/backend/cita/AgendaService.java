package com.alphabike.backend.cita;
import com.alphabike.backend.cita.dto.CitaRequest;
import com.alphabike.backend.servicio.Servicio;
import com.alphabike.backend.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.*;
import java.util.*;

@Service @RequiredArgsConstructor
public class AgendaService {
    private final CitaRepository citas;
    private final AgendaLockRepository lock;
    @Value("${app.agenda.apertura:}") private String apertura;
    @Value("${app.agenda.cierre:}") private String cierre;
    @Value("${app.agenda.dias:1,2,3,4,5,6,7}") private String dias;
    @Value("${app.agenda.capacidad:0}") private int capacidad;
    @jakarta.annotation.PostConstruct
    public void validarConfiguracion() {
        if (capacidad < 0 || apertura.isBlank() != cierre.isBlank()) throw new IllegalStateException("Configura apertura y cierre juntos, y capacidad no negativa");
        if (!apertura.isBlank() && !LocalTime.parse(apertura).isBefore(LocalTime.parse(cierre))) throw new IllegalStateException("La apertura debe ser anterior al cierre");
        if (dias.isBlank() || Arrays.stream(dias.split(",")).anyMatch(d -> !d.matches("[1-7]"))) throw new IllegalStateException("AGENDA_DIAS admite numeros del 1 al 7 separados por coma");
    }
    public record Config(String apertura, String cierre, String dias, int capacidad) {}
    public Config configuracion() { return new Config(apertura, cierre, dias, capacidad); }
    public void validar(CitaRequest request, Servicio servicio) {
        var inicio = LocalDateTime.of(request.getFecha(), request.getHora());
        var fin = inicio.plusMinutes(servicio.getDuracionMin());
        if (!Arrays.asList(dias.split(",")).contains(String.valueOf(request.getFecha().getDayOfWeek().getValue())))
            throw new BadRequestException("El taller no atiende ese dia");
        if (!apertura.isBlank() && !cierre.isBlank()
                && (inicio.toLocalTime().isBefore(LocalTime.parse(apertura))
                || fin.toLocalTime().isAfter(LocalTime.parse(cierre)) || !fin.toLocalDate().equals(inicio.toLocalDate())))
            throw new BadRequestException("El servicio debe completarse dentro del horario de atencion");
        if (capacidad <= 0) return;
        lock.bloquear().orElseThrow(() -> new IllegalStateException("Falta inicializar el control de agenda"));
        // Sweep line counts simultaneous appointments, not all overlaps as one group.
        var puntos = new TreeMap<LocalDateTime, Integer>();
        citas.findByFecha(request.getFecha()).stream()
                .filter(c -> c.getEstado() != Cita.Estado.CANCELADO)
                .forEach(c -> {
                    var desde = LocalDateTime.of(c.getFecha(), c.getHora());
                    var hasta = desde.plusMinutes(c.getServicio().getDuracionMin());
                    if (desde.isBefore(fin) && hasta.isAfter(inicio)) {
                        puntos.merge(desde.isBefore(inicio) ? inicio : desde, 1, Integer::sum);
                        puntos.merge(hasta.isAfter(fin) ? fin : hasta, -1, Integer::sum);
                    }
                });
        int atendiendo = 0;
        for (var punto : puntos.entrySet()) {
            atendiendo += punto.getValue();
            if (punto.getKey().isBefore(fin) && atendiendo >= capacidad)
                throw new BadRequestException("El horario seleccionado ya no tiene capacidad disponible");
        }
    }
}
