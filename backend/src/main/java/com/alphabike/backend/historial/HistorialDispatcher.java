package com.alphabike.backend.historial;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.historial.enabled", havingValue = "true", matchIfMissing = true)
public class HistorialDispatcher {
    private final EventoPendienteRepository pendientes;
    private final HistorialMongoService historial;

    @Scheduled(fixedDelayString = "${app.historial.interval-ms:2000}", initialDelayString = "${app.historial.initial-delay-ms:2000}")
    public void enviarPendientes() {
        for (EventoPendiente evento : pendientes.findTop50ByOrderByOcurridoEnAscIdAsc()) {
            try {
                historial.guardar(evento);
                // Separate SQL transaction after Mongo acknowledgement. A crash
                // between these writes safely retries the same document ID.
                pendientes.deleteById(evento.getId());
            } catch (RuntimeException ex) {
                log.warn("Historial pendiente de sincronizacion; se reintentara ({})", ex.getClass().getSimpleName());
                break;
            }
        }
    }
}

@Configuration
@EnableScheduling
class HistorialSchedulingConfiguration {}
