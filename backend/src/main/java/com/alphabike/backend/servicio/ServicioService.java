package com.alphabike.backend.servicio;

import com.alphabike.backend.servicio.dto.*;
import com.alphabike.backend.shared.exception.BadRequestException;
import com.alphabike.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicioService {

    private final ServicioRepository servicioRepository;

    public List<ServicioResponse> listar() {
        return servicioRepository.findAll()
                .stream()
                .map(ServicioResponse::from)
                .toList();
    }

    public ServicioResponse obtener(String id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));
        return ServicioResponse.from(servicio);
    }

    public ServicioResponse crear(ServicioRequest request) {
        if (servicioRepository.existsByNombre(request.getNombre())) {
            throw new BadRequestException("Ya existe un servicio con ese nombre");
        }
        Servicio servicio = Servicio.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .precioBase(request.getPrecioBase())
                .duracionMin(request.getDuracionMin())
                .build();
        return ServicioResponse.from(servicioRepository.save(servicio));
    }

    public ServicioResponse actualizar(String id, ServicioRequest request) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));
        servicio.setNombre(request.getNombre());
        servicio.setDescripcion(request.getDescripcion());
        servicio.setPrecioBase(request.getPrecioBase());
        servicio.setDuracionMin(request.getDuracionMin());
        return ServicioResponse.from(servicioRepository.save(servicio));
    }

    public void eliminar(String id) {
        if (!servicioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Servicio no encontrado");
        }
        servicioRepository.deleteById(id);
    }
}
