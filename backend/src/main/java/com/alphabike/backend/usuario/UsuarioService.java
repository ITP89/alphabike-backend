package com.alphabike.backend.usuario;

import com.alphabike.backend.cita.CitaRepository;
import com.alphabike.backend.cita.dto.CitaResponse;
import com.alphabike.backend.pedido.PedidoRepository;
import com.alphabike.backend.pedido.dto.PedidoResponse;
import com.alphabike.backend.shared.exception.BadRequestException;
import com.alphabike.backend.shared.exception.ResourceNotFoundException;
import com.alphabike.backend.shared.validation.EnumUtils;
import com.alphabike.backend.usuario.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PedidoRepository pedidoRepository;
    private final CitaRepository citaRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll()
                .stream()
                .map(UsuarioResponse::from)
                .toList();
    }

    public UsuarioResponse obtener(String id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return UsuarioResponse.from(usuario);
    }

    public UsuarioResponse crear(UsuarioRequest request) {
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("La contrasena es obligatoria");
        }

        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("El email ya esta registrado");
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .telefono(request.getTelefono())
                .rol(EnumUtils.parse(Usuario.Rol.class, request.getRol(), "rol"))
                .estado(Usuario.Estado.ACTIVO)
                .build();
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    public UsuarioResponse actualizar(String id, UsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        usuario.setNombre(request.getNombre());
        usuario.setTelefono(request.getTelefono());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    public UsuarioResponse cambiarEstado(String id, String estado) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        usuario.setEstado(EnumUtils.parse(Usuario.Estado.class, estado, "estado"));
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    public UsuarioResponse cambiarRol(String id, String rol) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        usuario.setRol(EnumUtils.parse(Usuario.Rol.class, rol, "rol"));
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    public UsuarioResponse actualizarPerfilPropio(String email, PerfilRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        usuario.setNombre(request.getNombre());
        usuario.setTelefono(request.getTelefono());
        if (request.getPasswordNueva() != null && !request.getPasswordNueva().isBlank()) {
            if (request.getPasswordActual() == null || request.getPasswordActual().isBlank()) {
                throw new BadRequestException("La contrasena actual es obligatoria");
            }
            if (!passwordEncoder.matches(request.getPasswordActual(), usuario.getPasswordHash())) {
                throw new BadRequestException("La contrasena actual no es correcta");
            }
            usuario.setPasswordHash(passwordEncoder.encode(request.getPasswordNueva()));
        }

        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    public HistorialClienteResponse historial(String id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<PedidoResponse> pedidos = pedidoRepository.findByClienteId(id)
                .stream()
                .map(PedidoResponse::from)
                .toList();

        List<CitaResponse> citas = citaRepository.findByClienteId(id)
                .stream()
                .map(CitaResponse::from)
                .toList();

        return HistorialClienteResponse.builder()
                .usuario(UsuarioResponse.from(usuario))
                .pedidos(pedidos)
                .citas(citas)
                .build();
    }
}
