package com.alphabike.backend.historial;

import com.alphabike.backend.realtime.CitaChanged;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.*;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CapturarHistorial {
    private final EventoPendienteRepository pendientes;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void capturar(CitaChanged cambio) {
        EventoPendiente evento = new EventoPendiente();
        evento.setId(UUID.randomUUID().toString());
        evento.setCitaId(cambio.citaId());
        evento.setEstado(cambio.estado());
        evento.setOcurridoEn(Instant.now());
        pendientes.save(evento);
    }
}
