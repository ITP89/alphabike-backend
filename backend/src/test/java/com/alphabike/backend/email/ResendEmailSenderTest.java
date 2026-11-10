package com.alphabike.backend.email;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;

import static org.assertj.core.api.Assertions.*;

class ResendEmailSenderTest {
    final ObjectMapper json = new ObjectMapper();

    @Test void verificationAndRecoveryUseHttpWithEscapedNamesAndFrontendLinks() throws Exception {
        var bodies = new LinkedBlockingQueue<JsonNode>();
        var authorization = new LinkedBlockingQueue<String>();
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/emails", exchange -> {
            authorization.add(exchange.getRequestHeaders().getFirst("Authorization"));
            bodies.add(json.readTree(exchange.getRequestBody()));
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().write("{\"id\":\"local-test\"}".getBytes(StandardCharsets.UTF_8));
            exchange.close();
        });
        server.start();
        try {
            var sender = new ResendEmailSender(RestClient.builder(), "local-test-key",
                    "http://127.0.0.1:" + server.getAddress().getPort());
            var service = new EmailService();
            ReflectionTestUtils.setField(service, "resendSender", sender);
            ReflectionTestUtils.setField(service, "provider", "resend");
            ReflectionTestUtils.setField(service, "frontendUrl", "https://frontend.example");
            ReflectionTestUtils.setField(service, "fromEmail", "AlphaBike <cuentas@example.test>");
            service.enviarVerificacionEmail("cliente@example.test", "<img src=x>", "verify-token");
            service.enviarRecuperacionPassword("cliente@example.test", "Cliente & prueba", "reset-token");
            assertThat(bodies).hasSize(2);
            var verification = bodies.remove();
            assertThat(verification.path("to").get(0).asText()).isEqualTo("cliente@example.test");
            assertThat(verification.path("html").asText()).contains("&lt;img src=x&gt;",
                    "https://frontend.example/verificar-email?token=verify-token").doesNotContain("<img src=x>");
            assertThat(bodies.remove().path("html").asText()).contains("Cliente &amp; prueba",
                    "https://frontend.example/restablecer-password?token=reset-token");
            assertThat(authorization).containsOnly("Bearer local-test-key");
        } finally { server.stop(0); }
    }

    @Test void missingKeyDoesNotAttemptNetworkAndProviderRejectionIsNotSuccess() throws Exception {
        var missing = new ResendEmailSender(RestClient.builder(), "", "http://127.0.0.1:1");
        assertThatThrownBy(() -> missing.send("from", "to", "subject", "html"))
                .isInstanceOf(IllegalStateException.class);
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/emails", exchange -> { exchange.sendResponseHeaders(403, -1); exchange.close(); });
        server.start();
        try {
            var rejected = new ResendEmailSender(RestClient.builder(), "invalid-test-key",
                    "http://127.0.0.1:" + server.getAddress().getPort());
            assertThatThrownBy(() -> rejected.send("from", "to", "subject", "html"))
                    .isInstanceOf(RestClientResponseException.class);
        } finally { server.stop(0); }
    }
}
