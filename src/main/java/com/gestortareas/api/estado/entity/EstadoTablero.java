package com.gestortareas.api.estado.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gestortareas.api.tablero.entity.Tablero;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "estados_tablero", schema = "gestortareas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoTablero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false)
    private Integer orden;

    @Column(name = "color_hex", length = 20)
    private String colorHex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tablero_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Tablero tablero;
}
