package com.alphabike.backend.proveedor.dto;

import com.alphabike.backend.proveedor.Proveedor;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProveedorResponse {

    private String id;
    private String nombre;
    private String ruc;
    private String telefono;
    private String email;
    private String direccion;
    private String contactoPrincipal;
    private String estado;

    public static ProveedorResponse from(Proveedor proveedor) {
        return ProveedorResponse.builder()
                .id(proveedor.getId())
                .nombre(proveedor.getNombre())
                .ruc(proveedor.getRuc())
                .telefono(proveedor.getTelefono())
                .email(proveedor.getEmail())
                .direccion(proveedor.getDireccion())
                .contactoPrincipal(proveedor.getContactoPrincipal())
                .estado(proveedor.getEstado().name())
                .build();
    }
}
