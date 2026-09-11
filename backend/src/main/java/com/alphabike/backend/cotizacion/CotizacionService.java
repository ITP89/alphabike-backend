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

    public CotizacionResponse obtener(String id, String emailUsuario) {
        Cotizacion cotizacion = cotizacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cotizacion no encontrada"));
        validarAccesoLectura(cotizacion, emailUsuario);
        return CotizacionResponse.from(cotizacion);
    }

    public CotizacionResponse obtenerPorCita(String citaId, String emailUsuario) {
        Cotizacion cotizacion = cotizacionRepository.findByCitaId(citaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cotizacion no encontrada para esta cita"));
        validarAccesoLectura(cotizacion, emailUsuario);
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

        if (usuario.getRol() == Usuario.Rol.CLIENTE) {
            if (nuevoEstado != Cotizacion.Estado.ACEPTADA && nuevoEstado != Cotizacion.Estado.RECHAZADA) {
                throw new UnauthorizedException("Solo puedes aceptar o rechazar esta cotizacion");
            }
            if (!cotizacion.getCita().getCliente().getId().equals(usuario.getId())) {
                throw new UnauthorizedException("No puedes modificar esta cotizacion");
            }
        }

        cotizacion.setEstado(nuevoEstado);
        return CotizacionResponse.from(cotizacionRepository.save(cotizacion));
    }

    private void validarAccesoLectura(Cotizacion cotizacion, String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        if (usuario.getRol() == Usuario.Rol.CLIENTE &&
                !cotizacion.getCita().getCliente().getId().equals(usuario.getId())) {
            throw new UnauthorizedException("No tienes permiso para ver esta cotizacion");
        }
    }
}
