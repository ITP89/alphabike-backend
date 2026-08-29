package com.alphabike.backend.producto;

import com.alphabike.backend.categoria.Categoria;
import com.alphabike.backend.categoria.CategoriaRepository;
import com.alphabike.backend.producto.dto.*;
import com.alphabike.backend.shared.exception.BadRequestException;
import com.alphabike.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public List<ProductoResponse> listar() {
        return productoRepository.findAll()
                .stream()
                .map(ProductoResponse::from)
                .toList();
    }

    public List<ProductoResponse> listarActivos() {
        return productoRepository.findByEstado(Producto.Estado.ACTIVO)
                .stream()
                .map(ProductoResponse::from)
                .toList();
    }

    public ProductoResponse obtener(String id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        return ProductoResponse.from(producto);
    }

    public ProductoResponse crear(ProductoRequest request) {
        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada"));

        Producto producto = Producto.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .marca(request.getMarca())
                .precio(request.getPrecio())
                .stock(request.getStock())
                .imagenUrl(request.getImagenUrl())
                .categoria(categoria)
                .estado(Producto.Estado.ACTIVO)
                .build();

        return ProductoResponse.from(productoRepository.save(producto));
    }

    public ProductoResponse actualizar(String id, ProductoRequest request) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada"));

        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setMarca(request.getMarca());
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock());
        producto.setImagenUrl(request.getImagenUrl());
        producto.setCategoria(categoria);

        return ProductoResponse.from(productoRepository.save(producto));
    }

    public ProductoResponse actualizarStock(String id, Integer stock) {
        if (stock == null || stock < 0) {
            throw new BadRequestException("El stock no puede ser negativo");
        }
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        producto.setStock(stock);
        return ProductoResponse.from(productoRepository.save(producto));
    }

    public void eliminar(String id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        producto.setEstado(Producto.Estado.DESCONTINUADO);
        productoRepository.save(producto);
    }
}
