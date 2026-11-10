package com.alphabike.backend;

import com.alphabike.backend.cita.CitaService;
import com.alphabike.backend.cita.dto.CitaRequest;
import com.alphabike.backend.cotizacion.CotizacionService;
import com.alphabike.backend.cotizacion.dto.CotizacionRequest;
import com.alphabike.backend.pago.PagoService;
import com.alphabike.backend.pago.dto.PagoRequest;
import com.alphabike.backend.servicio.*;
import com.alphabike.backend.usuario.*;
import com.alphabike.backend.shared.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.assertThat;

/** Real PostgreSQL lock contention: never run against customer databases. */
@SpringBootTest
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "TEST_DB_DRIVER", matches = "org.postgresql.Driver")
class TallerConcurrencyIntegrationTest {
    @Autowired CitaService citas;
    @Autowired CotizacionService cotizaciones;
    @Autowired PagoService pagos;
    @Autowired UsuarioRepository usuarios;
    @Autowired ServicioRepository servicios;
    @Autowired DataSource dataSource;
    @Autowired JdbcTemplate jdbc;

    record Fixture(String email, String id, String quote) {}

    Fixture crearCaso() {
        String email = UUID.randomUUID() + "@concurrency.test";
        usuarios.save(Usuario.builder().nombre("Prueba concurrente").email(email).telefono("999999999")
                .passwordHash("test-only").rol(Usuario.Rol.CLIENTE).estado(Usuario.Estado.ACTIVO)
                .emailVerificado(true).build());
        Servicio servicio = servicios.save(Servicio.builder().nombre("Concurrencia " + UUID.randomUUID())
                .precioBase(new BigDecimal("40.00")).duracionMin(30).build());
        String id = citas.crear(new CitaRequest(servicio.getId(), LocalDate.now().plusDays(2),
                LocalTime.NOON, "Bicicleta prueba", null), email).getId();
        String quote = cotizaciones.crearOActualizar(id,
                new CotizacionRequest("Mantenimiento", new BigDecimal("40.00"))).getId();

        return new Fixture(email, id, quote);
    }

    Runnable pago(Fixture f) {
        return () -> pagos.registrar(new PagoRequest("COTIZACION", f.quote(), new BigDecimal("40.00"),
                "EFECTIVO", null, null, null, null), f.email());
    }

    @Test void cancelarYPagarConcurrentementeNoPuedenConfirmarseAmbos() throws Exception {
        Fixture f = crearCaso();
        confirmarSoloUna(f.id(), () -> citas.cancelar(f.id(), f.email()), pago(f));
    }

    @Test void cambiarPrecioYPagarConcurrentementeNoPuedenConfirmarseAmbos() throws Exception {
        Fixture f = crearCaso();
        confirmarSoloUna(f.id(), () -> cotizaciones.crearOActualizar(f.id(),
                new CotizacionRequest("Precio actualizado", new BigDecimal("60.00"))), pago(f));
    }

    @Test void dosPagosConcurrentesSoloConfirmanUno() throws Exception {
        Fixture f = crearCaso();
        confirmarSoloUna(f.id(), pago(f), pago(f));
    }

    void confirmarSoloUna(String id, Runnable primera, Runnable segunda) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try (var holder = dataSource.getConnection()) {
            holder.setAutoCommit(false);
            try (var lock = holder.prepareStatement("select id from taller.citas where id = ? for update")) {
                lock.setString(1, id);
                lock.executeQuery().close();
            }
            Future<Boolean> cancelar = pool.submit(() -> resultado(primera));
            Future<Boolean> pagar = pool.submit(() -> resultado(segunda));
            // Both requests have reached the same locked appointment. With no
            // mutation lock they have already validated stale PENDIENTE data.
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            int waiting = 0;
            while (System.nanoTime() < deadline) {
                waiting = jdbc.queryForObject("select count(*) from pg_stat_activity where datname = current_database() and wait_event_type = 'Lock'", Integer.class);
                if (waiting >= 2) break;
                Thread.sleep(25);
            }
            holder.commit();
            assertThat(waiting).as("two concurrent operations reached PostgreSQL contention").isGreaterThanOrEqualTo(2);
            int confirmed = (cancelar.get(15, TimeUnit.SECONDS) ? 1 : 0) + (pagar.get(15, TimeUnit.SECONDS) ? 1 : 0);
            assertThat(confirmed).as("conflicting workshop operations cannot both commit").isEqualTo(1);
        } finally {
            pool.shutdownNow();
        }
    }

    private boolean resultado(Runnable operation) {
        try { operation.run(); return true; }
        catch (BadRequestException expectedConflict) { return false; }
    }
}
