package com.alphabike.backend.pago;

import com.alphabike.backend.pago.dto.*;
import com.alphabike.backend.pedido.Pedido;
import com.alphabike.backend.pedido.PedidoRepository;
import com.alphabike.backend.cotizacion.Cotizacion;
import com.alphabike.backend.cotizacion.CotizacionRepository;
import com.alphabike.backend.shared.exception.BadRequestException;
import com.alphabike.backend.shared.exception.ResourceNotFoundException;
import com.alphabike.backend.shared.exception.UnauthorizedException;
import com.alphabike.backend.shared.validation.EnumUtils;
import com.alphabike.backend.usuario.Usuario;
import com.alphabike.backend.usuario.UsuarioRepository;
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
    private final UsuarioRepository usuarioRepository;

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
    public PagoResponse registrar(PagoRequest request, String emailUsuario) {
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

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (pagoRepository.existsByReferenciaTipoAndReferenciaIdAndEstado(
                tipo, request.getReferenciaId(), Pago.Estado.PAGADO)) {
            throw new BadRequestException("Esta referencia ya tiene un pago registrado");
        }

        if (tipo == Pago.ReferenciaTipo.PEDIDO) {
            Pedido pedido = pedidoRepository.findById(request.getReferenciaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
            validarPropietario(usuario, pedido.getCliente().getId());
            if (pedido.getEstado() != Pedido.Estado.PENDIENTE) {
                throw new BadRequestException("El pedido no esta pendiente de pago");
            }
            if (request.getMonto().compareTo(pedido.getTotal()) != 0) {
                throw new BadRequestException("El monto debe coincidir exactamente con el total del pedido");
            }
            pedido.setEstado(Pedido.Estado.PAGADO);
            pedidoRepository.save(pedido);
        } else {
            Cotizacion cotizacion = cotizacionRepository.findById(request.getReferenciaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cotizacion no encontrada"));
            validarPropietario(usuario, cotizacion.getCita().getCliente().getId());
            if (cotizacion.getEstado() != Cotizacion.Estado.PENDIENTE) {
                throw new BadRequestException("La cotizacion no esta pendiente de pago");
            }
            if (request.getMonto().compareTo(cotizacion.getMonto()) != 0) {
                throw new BadRequestException("El monto debe coincidir exactamente con la cotizacion");
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
                .codigoAutorizacion(request.getCodigoAutorizacion())
                .transaccionId(request.getTransaccionId())
                .tarjetaMarca(request.getTarjetaMarca())
                .tarjetaUltimos4(request.getTarjetaUltimos4())
                .estado(Pago.Estado.PAGADO)
                .build();

        return PagoResponse.from(pagoRepository.save(pago));
    }

    private void validarPropietario(Usuario usuario, String propietarioId) {
        if (usuario.getRol() == Usuario.Rol.CLIENTE && !usuario.getId().equals(propietarioId)) {
            throw new UnauthorizedException("No tiene permiso para pagar esta referencia");
        }
    }
}
