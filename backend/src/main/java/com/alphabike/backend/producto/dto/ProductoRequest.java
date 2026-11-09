package com.alphabike.backend.producto.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El campo no puede superar 255 caracteres")
    private String nombre;

    @Size(max = 255, message = "El campo no puede superar 255 caracteres")
    private String descripcion;

    @NotBlank(message = "La marca es obligatoria")
    @Size(max = 255, message = "El campo no puede superar 255 caracteres")
    private String marca;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 8, fraction = 2, message = "El importe admite hasta 8 enteros y 2 decimales")
    private BigDecimal precio;

    @DecimalMin(value = "0.0", inclusive = false, message = "El precio minimo de venta debe ser mayor a cero")
    @Digits(integer = 8, fraction = 2, message = "El importe admite hasta 8 enteros y 2 decimales")
    private BigDecimal precioMinimoVenta;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0)
    private Integer stock;

    private String imagenUrl;

    @NotBlank(message = "La categoria es obligatoria")
    @Size(max = 255, message = "El campo no puede superar 255 caracteres")
    private String categoriaId;

    @Size(max = 255, message = "El campo no puede superar 255 caracteres")
    private String proveedorId;
}
