package com.gestortareas.api.checklist.entity;

import com.gestortareas.api.checklist.dto.ChecklistItemDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "checklist_items", schema = "gestortareas")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChecklistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private com.gestortareas.api.ticket.entity.Ticket ticket;

    @Column(nullable = false, length = 500)
    private String texto;

    @Column
    private Boolean completado = false;

    @Column
    private Short orden = 0;

    public ChecklistItemDTO toDTO() {
        return ChecklistItemDTO.builder()
                .id(this.id)
                .texto(this.texto)
                .completado(this.completado)
                .orden(this.orden)
                .build();
    }
}
