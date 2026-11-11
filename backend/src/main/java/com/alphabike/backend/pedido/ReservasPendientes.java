package com.alphabike.backend.pedido;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.*;
@Component @RequiredArgsConstructor @Slf4j
public class ReservasPendientes {
    private final PedidoRepository repository;
    private final PedidoService pedidos;
    @Value("${app.pedidos.reserva-minutos:0}") private int minutos;
    @Scheduled(fixedDelay = 60000)
    public void liberarVencidas() {
        if (minutos <= 0) return;
        var limite = LocalDateTime.now().minusMinutes(minutos);
        repository.findByEstado(Pedido.Estado.PENDIENTE).stream()
                .filter(p -> p.getMercadoPagoOrderId() == null && p.getFecha().isBefore(limite))
                .forEach(p -> { try { pedidos.liberarReservaVencida(p.getId(), limite); }
                    catch (RuntimeException e) { log.warn("No se pudo liberar una reserva vencida {}", p.getId()); } });
    }
}
