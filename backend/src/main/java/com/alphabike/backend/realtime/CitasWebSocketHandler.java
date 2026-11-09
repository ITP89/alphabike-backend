package com.alphabike.backend.realtime;

import com.alphabike.backend.security.JwtTokenProvider;
import com.alphabike.backend.usuario.Usuario;
import com.alphabike.backend.usuario.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class CitasWebSocketHandler extends TextWebSocketHandler {
    private final ObjectMapper mapper;
    private final JwtTokenProvider tokens;
    private final UsuarioRepository usuarios;
    private final Map<String, Connection> connections = new ConcurrentHashMap<>();

    private static class Connection {
        final WebSocketSession session;
        final long openedAt = System.currentTimeMillis();
        volatile String token;
        Connection(WebSocketSession session) {
            this.session = new ConcurrentWebSocketSessionDecorator(session, 3000, 65536);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        session.setTextMessageSizeLimit(8192);
        connections.put(session.getId(), new Connection(session));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        Connection connection = connections.get(session.getId());
        if (connection == null) return;
        try {
            var frame = mapper.readTree(message.getPayload());
            String type = frame.path("type").asText();
            if (connection.token == null) {
                if (!"AUTH".equals(type)) {
                    close(connection, CloseStatus.POLICY_VIOLATION);
                    return;
                }
                connection.token = frame.path("token").asText();
                if (authenticatedUser(connection) == null) {
                    close(connection, CloseStatus.POLICY_VIOLATION);
                    return;
                }
                send(connection, Map.of("type", "READY"));
                return;
            }
            if (authenticatedUser(connection) == null) {
                close(connection, CloseStatus.POLICY_VIOLATION);
                return;
            }
            switch (type) {
                case "PING" -> send(connection, Map.of("type", "PONG"));
                // Reconciliation after reconnect: the client reloads authorized REST snapshots.
                case "SYNC" -> send(connection, Map.of("type", "CITAS_SYNC"));
                default -> send(connection, Map.of("type", "ERROR", "message", "Evento no permitido"));
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
            close(connection, CloseStatus.BAD_DATA);
        }
    }

    private Usuario authenticatedUser(Connection connection) {
        if (connection.token == null || !tokens.validateToken(connection.token)) return null;
        return usuarios.findByEmail(tokens.getEmailFromToken(connection.token))
                .filter(u -> u.getEstado() == Usuario.Estado.ACTIVO && u.isEmailVerificado())
                .orElse(null);
    }

    @TransactionalEventListener
    public void onCitaChanged(CitaChanged event) {
        for (Connection connection : connections.values()) {
            if (connection.token == null) continue;
            try {
                Usuario user = authenticatedUser(connection);
                if (user == null) {
                    close(connection, CloseStatus.POLICY_VIOLATION);
                } else if (user.getRol() != Usuario.Rol.CLIENTE
                        || user.getEmail().equalsIgnoreCase(event.clienteEmail())) {
                    send(connection, Map.of("type", "CITA_UPDATED", "citaId", event.citaId(),
                            "estado", event.estado()));
                }
            } catch (IOException | RuntimeException ex) {
                // Notification failures must never turn a committed CRUD operation into HTTP 500.
                closeQuietly(connection);
            }
        }
    }

    @Scheduled(fixedDelay = 5000)
    public void expireUnauthenticatedConnections() {
        for (Connection connection : connections.values()) {
            if (connection.token == null && System.currentTimeMillis() - connection.openedAt > 5000) {
                closeQuietly(connection);
            }
        }
    }

    private void send(Connection connection, Map<String, String> frame) throws IOException {
        connection.session.sendMessage(new TextMessage(mapper.writeValueAsString(frame)));
    }

    private void close(Connection connection, CloseStatus status) throws IOException {
        connections.remove(connection.session.getId());
        connection.session.close(status);
    }

    private void closeQuietly(Connection connection) {
        try { close(connection, CloseStatus.POLICY_VIOLATION); } catch (IOException ignored) { }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        connections.remove(session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws IOException {
        Connection connection = connections.get(session.getId());
        if (connection != null) close(connection, CloseStatus.SERVER_ERROR);
    }
}
