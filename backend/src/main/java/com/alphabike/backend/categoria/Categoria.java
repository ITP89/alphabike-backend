package com.alphabike.backend.categoria;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categorias", schema = "tienda")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column
    private String descripcion;
}