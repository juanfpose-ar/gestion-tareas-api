package com.gestortareas.api.version.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gestortareas.api.tablero.entity.Tablero;
import com.gestortareas.api.ticket.entity.Ticket;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "versiones", schema = "gestortareas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Version {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String titulo;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDateTime fechaVencimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tablero_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Tablero tablero;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private VersionEstado estado = VersionEstado.POR_HACER;

    @OneToMany(mappedBy = "version", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Ticket> tickets = new ArrayList<>();
}
