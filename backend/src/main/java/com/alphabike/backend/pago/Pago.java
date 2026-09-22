package com.alphabike.backend.pago;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos", schema = "pagos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "referencia_tipo", nullable = false)
    private ReferenciaTipo referenciaTipo;

    @Column(name = "referencia_id", nullable = false)
    private String referenciaId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, columnDefinition = "VARCHAR(50)")
    private MetodoPago metodoPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    @Column(name = "codigo_autorizacion")
    private String codigoAutorizacion;

    @Column(name = "transaccion_id")
    private String transaccionId;

    @Column(name = "tarjeta_marca")
    private String tarjetaMarca;

    @Column(name = "tarjeta_ultimos4")
    private String tarjetaUltimos4;

    @PrePersist
    protected void onCreate() {
        this.fecha = LocalDateTime.now();
    }

    public enum ReferenciaTipo {
        PEDIDO, COTIZACION
    }

    public enum MetodoPago {
        EFECTIVO, YAPE, PLIN, TRANSFERENCIA, TARJETA
    }

    public enum Estado {
        PENDIENTE, PAGADO
    }
}