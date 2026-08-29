package com.alphabike.backend.pago;

import com.alphabike.backend.pago.dto.*;
import com.alphabike.backend.pedido.Pedido;
import com.alphabike.backend.pedido.PedidoRepository;
import com.alphabike.backend.cotizacion.Cotizacion;
import com.alphabike.backend.cotizacion.CotizacionRepository;
import com.alphabike.backend.shared.exception.BadRequestException;
import com.alphabike.backend.shared.exception.ResourceNotFoundException;
import com.alphabike.backend.shared.validation.EnumUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PagoService {

    private final PagoRepository pagoRepository;
    private final PedidoRepository pedidoRepository;
    private final CotizacionRepository cotizacionRepository;

    public List<PagoResponse> listar() {
        return pagoRepository.findAll()
                .stream()
                .map(PagoResponse::from)
                .toList();
    }

    public List<PagoResponse> listarPendientes() {
        return pagoRepository.findByEstado(Pago.Estado.PENDIENTE)
                .stream()
                .map(PagoResponse::from)
                .toList();
    }

    public List<PagoResponse> listarPorReferencia(String referenciaId) {
        return pagoRepository.findByReferenciaId(referenciaId)
                .stream()
                .map(PagoResponse::from)
                .toList();
    }

    @Transactional
    public PagoResponse registrar(PagoRequest request) {
        Pago.ReferenciaTipo tipo = EnumUtils.parse(
                Pago.ReferenciaTipo.class,
                request.getReferenciaTipo(),
                "referenciaTipo"
        );
        Pago.MetodoPago metodoPago = EnumUtils.parse(
                Pago.MetodoPago.class,
                request.getMetodoPago(),
                "metodoPago"
        );

        if (tipo == Pago.ReferenciaTipo.PEDIDO) {
            Pedido pedido = pedidoRepository.findById(request.getReferenciaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
            if (request.getMonto().compareTo(pedido.getTotal()) < 0) {
                throw new BadRequestException("El monto del pago no cubre el total del pedido");
            }
            pedido.setEstado(Pedido.Estado.PAGADO);
            pedidoRepository.save(pedido);
        } else {
            Cotizacion cotizacion = cotizacionRepository.findById(request.getReferenciaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cotizacion no encontrada"));
            if (request.getMonto().compareTo(cotizacion.getMonto()) < 0) {
                throw new BadRequestException("El monto del pago no cubre la cotizacion");
            }
            cotizacion.setEstado(Cotizacion.Estado.ACEPTADA);
            cotizacion.getCita().setEstado(
                    com.alphabike.backend.cita.Cita.Estado.COMPLETADO);
            cotizacionRepository.save(cotizacion);
        }

        Pago pago = Pago.builder()
                .referenciaTipo(tipo)
                .referenciaId(request.getReferenciaId())
                .monto(request.getMonto())
                .metodoPago(metodoPago)
                .estado(Pago.Estado.PAGADO)
                .build();

        return PagoResponse.from(pagoRepository.save(pago));
    }
}
