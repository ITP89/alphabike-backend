package com.alphabike.backend.cita;
import com.alphabike.backend.cita.dto.CitaRequest;
import com.alphabike.backend.servicio.Servicio;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.*;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
class AgendaServiceTest {
    CitaRepository citas = mock(CitaRepository.class);
    AgendaLockRepository lock = mock(AgendaLockRepository.class);
    AgendaService agenda = new AgendaService(citas, lock);
    Servicio servicio = Servicio.builder().duracionMin(60).build();
    LocalDate fecha = LocalDate.of(2026, 11, 16);
    @BeforeEach void setup() {
        ReflectionTestUtils.setField(agenda, "apertura", "09:00");
        ReflectionTestUtils.setField(agenda, "cierre", "18:00");
        ReflectionTestUtils.setField(agenda, "dias", "1,2,3,4,5,6");
    }
    CitaRequest request(String hora) { return new CitaRequest("s", fecha, LocalTime.parse(hora), "Bicicleta", null); }
    @Test void exigeQueTodaLaDuracionQuepaEnElHorario() {
        assertThatThrownBy(() -> agenda.validar(request("17:30"), servicio)).hasMessageContaining("horario");
        assertThatCode(() -> agenda.validar(request("17:00"), servicio)).doesNotThrowAnyException();
    }
    @Test void respetaCapacidadYPermiteCitasContiguas() {
        ReflectionTestUtils.setField(agenda, "capacidad", 1);
        when(lock.bloquear()).thenReturn(Optional.of(new AgendaLock()));
        when(citas.findByFecha(fecha)).thenReturn(List.of(Cita.builder().fecha(fecha).hora(LocalTime.of(10,0)).servicio(servicio).estado(Cita.Estado.PENDIENTE).build()));
        assertThatThrownBy(() -> agenda.validar(request("10:30"), servicio)).hasMessageContaining("capacidad");
        assertThatCode(() -> agenda.validar(request("11:00"), servicio)).doesNotThrowAnyException();
    }
}
