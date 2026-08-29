package com.alphabike.backend.categoria;

import com.alphabike.backend.categoria.dto.*;
import com.alphabike.backend.shared.exception.BadRequestException;
import com.alphabike.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAll()
                .stream()
                .map(CategoriaResponse::from)
                .toList();
    }

    public CategoriaResponse obtener(String id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada"));
        return CategoriaResponse.from(categoria);
    }

    public CategoriaResponse crear(CategoriaRequest request) {
        if (categoriaRepository.existsByNombre(request.getNombre())) {
            throw new BadRequestException("Ya existe una categoria con ese nombre");
        }
        Categoria categoria = Categoria.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .build();
        return CategoriaResponse.from(categoriaRepository.save(categoria));
    }

    public CategoriaResponse actualizar(String id, CategoriaRequest request) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada"));
        categoria.setNombre(request.getNombre());
        categoria.setDescripcion(request.getDescripcion());
        return CategoriaResponse.from(categoriaRepository.save(categoria));
    }

    public void eliminar(String id) {
        if (!categoriaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Categoria no encontrada");
        }
        categoriaRepository.deleteById(id);
    }
}
