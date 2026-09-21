package com.alphabike.backend.producto;

import com.alphabike.backend.categoria.Categoria;
import com.alphabike.backend.categoria.CategoriaRepository;
import com.alphabike.backend.producto.dto.*;
import com.alphabike.backend.proveedor.Proveedor;
import com.alphabike.backend.proveedor.ProveedorRepository;
import com.alphabike.backend.shared.exception.BadRequestException;
import com.alphabike.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProveedorRepository proveedorRepository;

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

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada"));
        Proveedor proveedor = resolveProveedor(request.getProveedorId());

        Producto producto = Producto.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .marca(request.getMarca())
                .precio(request.getPrecio())
                .precioMinimoVenta(resolvePrecioMinimoVenta(request))
                .stock(request.getStock())
                .imagenUrl(request.getImagenUrl())
                .categoria(categoria)
                .proveedor(proveedor)
                .estado(Producto.Estado.ACTIVO)
                .build();

        return ProductoResponse.from(productoRepository.save(producto));
    }

    @Transactional
    public ProductoResponse actualizar(String id, ProductoRequest request) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada"));
        Proveedor proveedor = resolveProveedor(request.getProveedorId());

        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setMarca(request.getMarca());
        producto.setPrecio(request.getPrecio());
        producto.setPrecioMinimoVenta(resolvePrecioMinimoVenta(request));
        producto.setStock(request.getStock());
        producto.setImagenUrl(request.getImagenUrl());
        producto.setCategoria(categoria);
        producto.setProveedor(proveedor);

        return ProductoResponse.from(productoRepository.save(producto));
    }

    @Transactional
    public ProductoResponse actualizarStock(String id, Integer stock) {
        if (stock == null || stock < 0) {
            throw new BadRequestException("El stock no puede ser negativo");
        }
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        producto.setStock(stock);
        return ProductoResponse.from(productoRepository.save(producto));
    }

    @Transactional
    public void eliminar(String id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        producto.setEstado(Producto.Estado.DESCONTINUADO);
        productoRepository.save(producto);
    }

    private BigDecimal resolvePrecioMinimoVenta(ProductoRequest request) {
        BigDecimal precioMinimo = request.getPrecioMinimoVenta() != null
                ? request.getPrecioMinimoVenta()
                : request.getPrecio();

        if (precioMinimo.compareTo(request.getPrecio()) > 0) {
            throw new BadRequestException("El precio minimo de venta no puede superar el precio normal");
        }

        return precioMinimo;
    }

    private Proveedor resolveProveedor(String proveedorId) {
        if (proveedorId == null || proveedorId.isBlank()) {
            return null;
        }

        Proveedor proveedor = proveedorRepository.findById(proveedorId)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));

        if (proveedor.getEstado() != Proveedor.Estado.ACTIVO) {
            throw new BadRequestException("El proveedor seleccionado esta inactivo");
        }

        return proveedor;
    }
}
