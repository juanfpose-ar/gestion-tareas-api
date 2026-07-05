package com.gestortareas.api.reunion.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gestortareas.api.tablero.entity.Tablero;
import com.gestortareas.api.ticket.entity.Ticket;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reuniones", schema = "gestortareas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reunion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", length = 5)
    private String horaInicio;

    @Column(name = "hora_fin", length = 5)
    private String horaFin;

    @Column(length = 7)
    private String color;

    @Column(name = "recordatorio_minutos")
    private Integer recordatorioMinutos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tablero_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Tablero tablero;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "reunion_tickets",
        schema = "gestortareas",
        joinColumns = @JoinColumn(name = "reunion_id"),
        inverseJoinColumns = @JoinColumn(name = "ticket_id")
    )
    @Builder.Default
    private List<Ticket> tickets = new ArrayList<>();
}
