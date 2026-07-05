package com.gestortareas.api.etiqueta.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gestortareas.api.tablero.entity.Tablero;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "etiquetas", schema = "gestortareas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Etiqueta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(length = 20)
    private String color;

    @Column(name = "color_texto", length = 10)
    private String colorTexto; // "#ffffff" o "#000000"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tablero_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Tablero tablero; // Si es null, es una etiqueta global (ADMIN)
}
