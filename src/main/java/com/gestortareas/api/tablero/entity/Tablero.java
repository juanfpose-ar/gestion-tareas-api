package com.gestortareas.api.tablero.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tableros", schema = "gestortareas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tablero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String titulo;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "imagen_fondo_url", length = 500)
    private String imagenFondoUrl;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    @Builder.Default
    private boolean archivado = false;

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
}
