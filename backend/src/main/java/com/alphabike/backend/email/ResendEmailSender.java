package com.alphabike.backend.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/** HTTPS transport for hosts that block outbound SMTP. Never logs provider responses or credentials. */
@Component
public class ResendEmailSender {
    private final RestClient client;
    private final String apiKey;

    public ResendEmailSender(RestClient.Builder builder,
                            @Value("${app.mail.resend.api-key:}") String apiKey,
                            @Value("${app.mail.resend.base-url:https://api.resend.com}") String baseUrl) {
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5)).build());
        factory.setReadTimeout(Duration.ofSeconds(10));
        this.client = builder.requestFactory(factory).baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    public void send(String from, String to, String subject, String html) {
        if (apiKey.isBlank()) throw new IllegalStateException("RESEND_API_KEY no configurada");
        client.post().uri("/emails")
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("from", from, "to", List.of(to), "subject", subject, "html", html))
                .retrieve().toBodilessEntity();
    }
}
