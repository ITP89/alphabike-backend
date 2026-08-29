package com.alphabike.backend.cotizacion;

import com.alphabike.backend.cita.Cita;
import com.alphabike.backend.cita.CitaRepository;
import com.alphabike.backend.cotizacion.dto.*;
import com.alphabike.backend.shared.exception.ResourceNotFoundException;
import com.alphabike.backend.shared.exception.UnauthorizedException;
import com.alphabike.backend.shared.validation.EnumUtils;
import com.alphabike.backend.usuario.Usuario;
import com.alphabike.backend.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CotizacionService {

    private final CotizacionRepository cotizacionRepository;
    private final CitaRepository citaRepository;
    private final UsuarioRepository usuarioRepository;

    public CotizacionResponse obtener(String id) {
        Cotizacion cotizacion = cotizacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cotizacion no encontrada"));
        return CotizacionResponse.from(cotizacion);
    }

    public CotizacionResponse obtenerPorCita(String citaId) {
        Cotizacion cotizacion = cotizacionRepository.findByCitaId(citaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cotizacion no encontrada para esta cita"));
        return CotizacionResponse.from(cotizacion);
    }

    public CotizacionResponse crearOActualizar(String citaId, CotizacionRequest request) {
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));

        Cotizacion cotizacion = cotizacionRepository.findByCitaId(citaId)
                .orElse(Cotizacion.builder().cita(cita).build());

        cotizacion.setDescripcion(request.getDescripcion());
        cotizacion.setMonto(request.getMonto());
        cotizacion.setEstado(Cotizacion.Estado.PENDIENTE);

        return CotizacionResponse.from(cotizacionRepository.save(cotizacion));
    }

    public CotizacionResponse cambiarEstado(String id, String estado, String emailUsuario) {
        Cotizacion cotizacion = cotizacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cotizacion no encontrada"));

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Cotizacion.Estado nuevoEstado = EnumUtils.parse(Cotizacion.Estado.class, estado, "estado");

        if (nuevoEstado == Cotizacion.Estado.ACEPTADA || nuevoEstado == Cotizacion.Estado.RECHAZADA) {
            if (usuario.getRol() != Usuario.Rol.CLIENTE) {
                throw new UnauthorizedException("Solo el cliente puede aceptar o rechazar");
            }
            if (!cotizacion.getCita().getCliente().getId().equals(usuario.getId())) {
                throw new UnauthorizedException("No puedes modificar esta cotizacion");
            }
        }

        cotizacion.setEstado(nuevoEstado);
        return CotizacionResponse.from(cotizacionRepository.save(cotizacion));
    }
}
