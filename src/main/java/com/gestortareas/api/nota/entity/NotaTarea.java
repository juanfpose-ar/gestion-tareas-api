package com.gestortareas.api.nota.entity;

import java.time.LocalDateTime;

import com.gestortareas.api.nota.dto.NotaTareaDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "notas_tarea", schema = "gestortareas")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotaTarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private com.gestortareas.api.ticket.entity.Ticket ticket;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String texto;

    @Column(nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    public NotaTareaDTO toDTO() {
        return NotaTareaDTO.builder()
                .id(this.id)
                .texto(this.texto)
                .fechaHora(this.fechaHora)
                .build();
    }
}
