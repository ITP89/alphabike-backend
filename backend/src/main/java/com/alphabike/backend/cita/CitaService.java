package com.alphabike.backend.cita;

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

import java.util.List;

@Service
@RequiredArgsConstructor
public class CitaService {

    private final CitaRepository citaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ServicioRepository servicioRepository;

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

    public CitaResponse obtener(String id) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));
        return CitaResponse.from(cita);
    }

    public CitaResponse crear(CitaRequest request, String clienteEmail) {
        Usuario cliente = usuarioRepository.findByEmail(clienteEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        Servicio servicio = servicioRepository.findById(request.getServicioId())
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        Cita cita = Cita.builder()
                .cliente(cliente)
                .servicio(servicio)
                .fecha(request.getFecha())
                .hora(request.getHora())
                .biciDescripcion(request.getBiciDescripcion())
                .observaciones(request.getObservaciones())
                .estado(Cita.Estado.PENDIENTE)
                .build();

        return CitaResponse.from(citaRepository.save(cita));
    }

    public CitaResponse cambiarEstado(String id, String estado) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));
        cita.setEstado(EnumUtils.parse(Cita.Estado.class, estado, "estado"));
        return CitaResponse.from(citaRepository.save(cita));
    }

    public CitaResponse asignarEncargado(String id, String encargadoId) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));
        Usuario encargado = usuarioRepository.findById(encargadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Encargado no encontrado"));
        if (encargado.getRol() != Usuario.Rol.ENCARGADO) {
            throw new UnauthorizedException("El usuario no es un encargado");
        }
        cita.setEncargado(encargado);
        return CitaResponse.from(citaRepository.save(cita));
    }

    public void cancelar(String id, String emailUsuario) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        if (usuario.getRol() == Usuario.Rol.CLIENTE &&
                !cita.getCliente().getId().equals(usuario.getId())) {
            throw new UnauthorizedException("No puedes cancelar esta cita");
        }
        cita.setEstado(Cita.Estado.CANCELADO);
        citaRepository.save(cita);
    }
}
