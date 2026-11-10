package com.alphabike.backend;

import com.alphabike.backend.cita.*;
import com.alphabike.backend.cita.dto.CitaRequest;
import com.alphabike.backend.historial.*;
import com.alphabike.backend.realtime.CitaChanged;
import com.alphabike.backend.security.JwtTokenProvider;
import com.alphabike.backend.servicio.*;
import com.alphabike.backend.usuario.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Runs against a real MongoDB server, plus H2 or PostgreSQL through TEST_DB_*.
 * No Mongo mock or in-memory replacement is used. */
@SpringBootTest(properties = {
        "app.historial.enabled=true", "spring.autoconfigure.exclude=",
        "spring.data.mongodb.uri=${MONGO_TEST_URI}",
        "app.historial.initial-delay-ms=3600000"})
@ActiveProfiles("test")
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "MONGO_TEST_URI", matches = ".+")
class HistorialMongoIntegrationTest {
    @Autowired CitaService citas;
    @Autowired UsuarioRepository usuarios;
    @Autowired ServicioRepository servicios;
    @Autowired EventoPendienteRepository pendientes;
    @Autowired HistorialDispatcher dispatcher;
    @Autowired HistorialMongoService historial;
    @Autowired MongoTemplate mongo;
    @Autowired PlatformTransactionManager transactions;
    @Autowired ApplicationEventPublisher events;
    @Autowired JwtTokenProvider tokens;
    @Autowired MockMvc http;
    String email;
    String citaId;

    void sincronizar() {
        long anteriores = pendientes.count();
        while (anteriores > 0) {
            dispatcher.enviarPendientes();
            long restantes = pendientes.count();
            assertThat(restantes).isLessThan(anteriores);
            anteriores = restantes;
        }
    }

    @BeforeEach void setup() {
        email = UUID.randomUUID() + "@mongo.test";
        usuarios.save(Usuario.builder().nombre("Cliente Mongo").email(email).telefono("999999999")
                .passwordHash("not-used-by-this-test").rol(Usuario.Rol.CLIENTE)
                .estado(Usuario.Estado.ACTIVO).emailVerificado(true).build());
        Servicio servicio = servicios.save(Servicio.builder().nombre("Mongo " + UUID.randomUUID())
                .precioBase(new BigDecimal("40.00")).duracionMin(30).build());
        citaId = citas.crear(new CitaRequest(servicio.getId(), LocalDate.now().plusDays(2),
                LocalTime.NOON, "Bicicleta de prueba", null), email).getId();
    }

    @Test void persistsRealDocumentsAndProtectsHistoryByOwner() throws Exception {
        assertThat(pendientes.findAll()).anyMatch(e -> e.getCitaId().equals(citaId));
        sincronizar();
        assertThat(historial.listar(citaId, 0)).extracting(EventoCita::estado).containsExactly("PENDIENTE");
        citas.cambiarEstado(citaId, "EN_PROCESO");
        sincronizar();
        assertThat(historial.listar(citaId, 0)).extracting(EventoCita::estado)
                .containsExactly("EN_PROCESO", "PENDIENTE");
        String token = tokens.generateToken(email, "CLIENTE");
        http.perform(get("/api/v1/citas/{id}/historial", citaId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data[0].estado").value("EN_PROCESO"));
        String other = UUID.randomUUID() + "@mongo.test";
        usuarios.save(Usuario.builder().nombre("Otro").email(other).telefono("999999998")
                .passwordHash("unused").rol(Usuario.Rol.CLIENTE).estado(Usuario.Estado.ACTIVO)
                .emailVerificado(true).build());
        http.perform(get("/api/v1/citas/{id}/historial", citaId)
                .header("Authorization", "Bearer " + tokens.generateToken(other, "CLIENTE")))
                .andExpect(status().isForbidden());
        http.perform(get("/api/v1/citas/{id}/historial", citaId))
                .andExpect(status().isForbidden());
        http.perform(get("/api/v1/citas/{id}/historial?pagina=-1", citaId)
                .header("Authorization", "Bearer " + token)).andExpect(status().isBadRequest());
    }

    @Test void rollbackNeverEnqueuesAnEventAndRetryDoesNotDuplicateMongoDocument() {
        long count = pendientes.count();
        new TransactionTemplate(transactions).executeWithoutResult(tx -> {
            events.publishEvent(new CitaChanged(citaId, email, "CANCELADO"));
            tx.setRollbackOnly();
        });
        assertThat(pendientes.count()).isEqualTo(count);
        EventoPendiente evento = pendientes.findAll().stream()
                .filter(e -> e.getCitaId().equals(citaId)).findFirst().orElseThrow();
        historial.guardar(evento); // Simulates Mongo ACK followed by a process crash.
        sincronizar();
        assertThat(historial.listar(citaId, 0)).hasSize(1);
        assertThat(pendientes.existsById(evento.getId())).isFalse();
        assertThat(mongo.getCollection("cita_eventos").find(
                new org.bson.Document("_id", evento.getId())).first())
                .doesNotContainKeys("clienteEmail", "passwordHash");
    }
}
