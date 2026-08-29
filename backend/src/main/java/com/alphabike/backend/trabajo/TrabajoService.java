package com.alphabike.backend.trabajo;

import com.alphabike.backend.cita.Cita;
import com.alphabike.backend.cita.CitaRepository;
import com.alphabike.backend.shared.exception.ResourceNotFoundException;
import com.alphabike.backend.trabajo.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrabajoService {

    private final TrabajoRealizadoRepository trabajoRepository;
    private final CitaRepository citaRepository;

    public List<TrabajoResponse> listar() {
        return trabajoRepository.findAll()
                .stream()
                .map(TrabajoResponse::from)
                .toList();
    }

    public List<TrabajoResponse> listarDestacados() {
        return trabajoRepository.findByDestacado(true)
                .stream()
                .map(TrabajoResponse::from)
                .toList();
    }

    public TrabajoResponse obtener(String id) {
        TrabajoRealizado trabajo = trabajoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trabajo no encontrado"));
        return TrabajoResponse.from(trabajo);
    }

    public TrabajoResponse crear(TrabajoRequest request) {
        Cita cita = null;
        if (request.getCitaId() != null) {
            cita = citaRepository.findById(request.getCitaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));
        }

        TrabajoRealizado trabajo = TrabajoRealizado.builder()
                .cita(cita)
                .titulo(request.getTitulo())
                .descripcion(request.getDescripcion())
                .imagenAntesUrl(request.getImagenAntesUrl())
                .imagenDespuesUrl(request.getImagenDespuesUrl())
                .destacado(request.getDestacado() != null ? request.getDestacado() : false)
                .build();

        return TrabajoResponse.from(trabajoRepository.save(trabajo));
    }

    public TrabajoResponse actualizar(String id, TrabajoRequest request) {
        TrabajoRealizado trabajo = trabajoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trabajo no encontrado"));

        trabajo.setTitulo(request.getTitulo());
        trabajo.setDescripcion(request.getDescripcion());
        trabajo.setImagenAntesUrl(request.getImagenAntesUrl());
        trabajo.setImagenDespuesUrl(request.getImagenDespuesUrl());
        if (request.getDestacado() != null) {
            trabajo.setDestacado(request.getDestacado());
        }

        return TrabajoResponse.from(trabajoRepository.save(trabajo));
    }

    public void eliminar(String id) {
        if (!trabajoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Trabajo no encontrado");
        }
        trabajoRepository.deleteById(id);
    }
}