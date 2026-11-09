package com.alphabike.backend.config;

import com.alphabike.backend.realtime.CitasWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.*;

import java.util.Arrays;

@Configuration
@EnableWebSocket
@EnableScheduling
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    private final CitasWebSocketHandler handler;

    @Value("${app.websocket.allowed-origins:${cors.allowed-origins}}")
    private String allowedOrigins;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/citas").setAllowedOrigins(
                Arrays.stream(allowedOrigins.split(",")).map(String::trim)
                        .filter(origin -> !origin.isBlank()).toArray(String[]::new));
    }
}
