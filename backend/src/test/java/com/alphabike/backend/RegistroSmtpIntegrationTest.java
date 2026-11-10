package com.alphabike.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.*;
import org.springframework.test.web.servlet.MockMvc;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Real SMTP protocol to a loopback capture server. Sends nothing to external recipients. */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class RegistroSmtpIntegrationTest {
    static final ServerSocket server;
    static final BlockingQueue<String> messages = new LinkedBlockingQueue<>();
    static {
        try { server = new ServerSocket(0, 10, InetAddress.getByName("127.0.0.1")); }
        catch (IOException e) { throw new ExceptionInInitializerError(e); }
        Thread smtp = new Thread(() -> {
            while (!server.isClosed()) {
                try (Socket socket = server.accept()) {
                    socket.setSoTimeout(10000);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                    Writer out = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
                    reply(out, "220 localhost test SMTP");
                    String line;
                    while ((line = in.readLine()) != null) {
                        if (line.startsWith("EHLO") || line.startsWith("HELO")) reply(out, "250 localhost");
                        else if (line.equals("DATA")) {
                            reply(out, "354 End with dot");
                            StringBuilder raw = new StringBuilder();
                            while ((line = in.readLine()) != null && !line.equals(".")) {
                                raw.append(line.startsWith("..") ? line.substring(1) : line).append("\r\n");
                            }
                            messages.add(raw.toString());
                            reply(out, "250 Accepted locally");
                        } else if (line.equals("QUIT")) { reply(out, "221 Bye"); break; }
                        else reply(out, "250 OK");
                    }
                } catch (IOException ignored) { /* Closing the fixture unblocks accept. */ }
            }
        }, "loopback-smtp-test");
        smtp.setDaemon(true);
        smtp.start();
    }

    static void reply(Writer out, String line) throws IOException { out.write(line + "\r\n"); out.flush(); }

    @DynamicPropertySource static void configure(DynamicPropertyRegistry props) {
        props.add("spring.mail.host", () -> "127.0.0.1");
        props.add("spring.mail.port", server::getLocalPort);
        props.add("spring.mail.properties.mail.smtp.auth", () -> false);
        props.add("spring.mail.properties.mail.smtp.starttls.enable", () -> false);
        props.add("app.frontend.url", () -> "http://localhost:5173");
    }

    @AfterAll static void close() throws IOException { server.close(); }
    @Autowired MockMvc http;
    @Autowired ObjectMapper json;

    @Test void registroEnviaSmtpYElEnlacePermiteVerificarEIniciarSesion() throws Exception {
        String email = UUID.randomUUID() + "@smtp.test";
        var body = Map.of("nombre", "Cliente prueba", "email", email,
                "password", "Prueba123!", "telefono", "999999999");
        http.perform(post("/api/v1/auth/register").contentType("application/json").content(json.writeValueAsString(body)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.emailVerificado").value(false));
        String raw = messages.poll(10, TimeUnit.SECONDS);
        assertThat(raw).as("message received over a real local SMTP socket").isNotNull();
        MimeMessage message = new MimeMessage(Session.getInstance(new Properties()),
                new ByteArrayInputStream(raw.getBytes(StandardCharsets.UTF_8)));
        assertThat(message.getAllRecipients()[0].toString()).isEqualTo(email);
        assertThat(message.getSubject()).contains("Activa tu cuenta");
        var matcher = Pattern.compile("http://localhost:5173/verificar-email\\?token=([a-f0-9-]+)").matcher(text(message));
        assertThat(matcher.find()).as("activation link in decoded MIME body").isTrue();
        String token = matcher.group(1);
        String login = json.writeValueAsString(Map.of("email", email, "password", "Prueba123!"));
        http.perform(post("/api/v1/auth/login").contentType("application/json").content(login))
                .andExpect(status().isBadRequest());
        http.perform(get("/api/v1/auth/verify-email").param("token", token)).andExpect(status().isOk());
        http.perform(post("/api/v1/auth/login").contentType("application/json").content(login))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.emailVerificado").value(true));
        http.perform(get("/api/v1/auth/verify-email").param("token", token)).andExpect(status().isNotFound());
    }

    static String text(Part part) throws Exception {
        Object content = part.getContent();
        if (content instanceof String value) return value;
        if (content instanceof Multipart parts) {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < parts.getCount(); i++) result.append(text(parts.getBodyPart(i)));
            return result.toString();
        }
        return "";
    }
}
