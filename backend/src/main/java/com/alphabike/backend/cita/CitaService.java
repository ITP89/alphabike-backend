package com.alphabike.backend.cita;

import com.alphabike.backend.realtime.CitaChanged;
import com.alphabike.backend.shared.exception.BadRequestException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.context.ApplicationEventPublisher;

import com.alphabike.backend.cita.dto.*;
import com.alphabike.backend.servicio.Servicio;
import com.alphabike.backend.servicio.ServicioRepository;
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
@Transactional
@RequiredArgsConstructor
public class CitaService {

    private final CitaRepository citaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ServicioRepository servicioRepository;
    private final ApplicationEventPublisher events;
    private final AgendaService agenda;

    private CitaResponse guardarYNotificar(Cita cita) {
        Cita guardada = citaRepository.save(cita);
        events.publishEvent(new CitaChanged(
                guardada.getId(), guardada.getCliente().getEmail(), guardada.getEstado().name()));
        return CitaResponse.from(guardada);
    }

    public List<CitaResponse> listar() {
        return citaRepository.findAll()
                .stream()
                .map(CitaResponse::from)
                .toList();
    }

    public List<CitaResponse> listarPorCliente(String clienteEmail) {
        Usuario cliente = usuarioRepository.findByEmail(clienteEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        return citaRepository.findByClienteId(cliente.getId())
                .stream()
                .map(CitaResponse::from)
                .toList();
    }

    public CitaResponse obtener(String id, String emailUsuario) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        if (usuario.getRol() == Usuario.Rol.CLIENTE &&
                !cita.getCliente().getId().equals(usuario.getId())) {
            throw new UnauthorizedException("No tienes permiso para ver esta cita");
        }
        return CitaResponse.from(cita);
    }

    public CitaResponse crear(CitaRequest request, String clienteEmail) {
        if (LocalDateTime.of(request.getFecha(), request.getHora())
                .isBefore(LocalDateTime.now(ZoneId.of("America/Lima")))) {
            throw new BadRequestException("La cita debe tener una fecha y hora futura");
        }
        Usuario cliente = usuarioRepository.findByEmail(clienteEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        Servicio servicio = servicioRepository.findById(request.getServicioId())
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        agenda.validar(request, servicio);
        Cita cita = Cita.builder()
                .cliente(cliente)
                .servicio(servicio)
                .fecha(request.getFecha())
                .hora(request.getHora())
                .biciDescripcion(request.getBiciDescripcion())
                .observaciones(request.getObservaciones())
                .estado(Cita.Estado.PENDIENTE)
                .build();

        return guardarYNotificar(cita);
    }

    public CitaResponse cambiarEstado(String id, String estado) {
        Cita cita = citaRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));
        Cita.Estado nuevoEstado = EnumUtils.parse(Cita.Estado.class, estado, "estado");
        if (nuevoEstado != cita.getEstado() && (cita.getEstado() == Cita.Estado.CANCELADO
                || cita.getEstado() == Cita.Estado.COMPLETADO
                || (cita.getEstado() == Cita.Estado.EN_PROCESO && nuevoEstado == Cita.Estado.PENDIENTE))) {
            throw new BadRequestException("La transicion de estado de la cita no esta permitida");
        }
        cita.setEstado(nuevoEstado);
        return guardarYNotificar(cita);
    }

    public CitaResponse asignarEncargado(String id, String encargadoId) {
        Cita cita = citaRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));
        Usuario encargado = usuarioRepository.findById(encargadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Encargado no encontrado"));
        if (cita.getEstado() == Cita.Estado.CANCELADO || cita.getEstado() == Cita.Estado.COMPLETADO) {
            throw new BadRequestException("La cita ya esta cerrada");
        }
        if (encargado.getRol() != Usuario.Rol.ENCARGADO || encargado.getEstado() != Usuario.Estado.ACTIVO) {
            throw new UnauthorizedException("El usuario no es un encargado");
        }
        cita.setEncargado(encargado);
        return guardarYNotificar(cita);
    }

    public CitaResponse tomar(String id, String email) {
        Cita cita = citaRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));
        Usuario encargado = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Encargado no encontrado"));
        if (cita.getEncargado() != null && !cita.getEncargado().getId().equals(encargado.getId())) {
            throw new BadRequestException("La cita ya tiene un encargado asignado");
        }
        return asignarEncargado(id, encargado.getId());
    }

    public void cancelar(String id, String emailUsuario) {
        Cita cita = citaRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        if (usuario.getRol() == Usuario.Rol.CLIENTE &&
                !cita.getCliente().getId().equals(usuario.getId())) {
            throw new UnauthorizedException("No puedes cancelar esta cita");
        }
        if (cita.getEstado() == Cita.Estado.COMPLETADO) {
            throw new BadRequestException("No se puede cancelar una cita completada");
        }
        cita.setEstado(Cita.Estado.CANCELADO);
        guardarYNotificar(cita);
    }
}
