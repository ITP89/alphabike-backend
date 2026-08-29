package com.alphabike.backend.categoria.dto;

import com.alphabike.backend.categoria.Categoria;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaResponse {

    private String id;
    private String nombre;
    private String descripcion;

    public static CategoriaResponse from(Categoria categoria) {
        return CategoriaResponse.builder()
                .id(categoria.getId())
                .nombre(categoria.getNombre())
                .descripcion(categoria.getDescripcion())
                .build();
    }
}