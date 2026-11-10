package com.alphabike.backend.historial;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.mockito.Mockito.*;

class HistorialDispatcherTest {
    @Test void mongoFailureKeepsSqlEventForRetry() {
        EventoPendienteRepository pendientes = mock(EventoPendienteRepository.class);
        HistorialMongoService mongo = mock(HistorialMongoService.class);
        EventoPendiente evento = new EventoPendiente();
        evento.setId("retry-id");
        when(pendientes.findTop50ByOrderByOcurridoEnAscIdAsc()).thenReturn(List.of(evento));
        doThrow(new org.springframework.dao.DataAccessResourceFailureException("offline"))
                .doNothing().when(mongo).guardar(evento);
        HistorialDispatcher dispatcher = new HistorialDispatcher(pendientes, mongo);
        dispatcher.enviarPendientes();
        verify(pendientes, never()).deleteById(anyString());
        dispatcher.enviarPendientes();
        verify(pendientes).deleteById("retry-id");
        verify(mongo, times(2)).guardar(evento);
    }
}
