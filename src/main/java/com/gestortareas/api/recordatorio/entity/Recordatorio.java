package com.gestortareas.api.recordatorio.entity;

import java.time.LocalDateTime;

import com.gestortareas.api.enums.TipoRecordatorio;
import com.gestortareas.api.recordatorio.dto.RecordatorioDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "recordatorios", schema = "gestortareas")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Recordatorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private com.gestortareas.api.ticket.entity.Ticket ticket;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoRecordatorio tipo;

    @Column
    private LocalDateTime fechaPersonalizada;

    @Column(nullable = false)
    private Boolean notificado = false;

    public RecordatorioDTO toDTO() {
        return RecordatorioDTO.builder()
                .id(this.id)
                .tipo(this.tipo)
                .fechaPersonalizada(this.fechaPersonalizada)
                .notificado(this.notificado)
                .build();
    }
}
