package com.alphabike.backend.producto.dto;

import com.alphabike.backend.producto.Producto;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoResponse {

    private String id;
    private String nombre;
    private String descripcion;
    private String marca;
    private BigDecimal precio;
    private BigDecimal precioMinimoVenta;
    private Integer stock;
    private String imagenUrl;
    private String estado;
    private String categoriaId;
    private String categoriaNombre;
    private String proveedorId;
    private String proveedorNombre;

    public static ProductoResponse from(Producto producto) {
        return ProductoResponse.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .marca(producto.getMarca())
                .precio(producto.getPrecio())
                .precioMinimoVenta(producto.getPrecioMinimoVenta())
                .stock(producto.getStock())
                .imagenUrl(producto.getImagenUrl())
                .estado(producto.getEstado().name())
                .categoriaId(producto.getCategoria().getId())
                .categoriaNombre(producto.getCategoria().getNombre())
                .proveedorId(producto.getProveedor() != null ? producto.getProveedor().getId() : null)
                .proveedorNombre(producto.getProveedor() != null ? producto.getProveedor().getNombre() : null)
                .build();
    }
}
