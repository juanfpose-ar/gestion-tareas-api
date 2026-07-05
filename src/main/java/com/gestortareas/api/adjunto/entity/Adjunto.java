package com.gestortareas.api.adjunto.entity;

import java.time.LocalDateTime;

import com.gestortareas.api.adjunto.dto.AdjuntoDTO;
import com.gestortareas.api.enums.TipoAdjunto;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "adjuntos", schema = "gestortareas")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Adjunto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private com.gestortareas.api.ticket.entity.Ticket ticket;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoAdjunto tipo;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(length = 255)
    private String nombre;

    @Column(nullable = false)
    private LocalDateTime fechaSubida = LocalDateTime.now();

    public AdjuntoDTO toDTO() {
        return AdjuntoDTO.builder()
                .id(this.id)
                .tipo(this.tipo)
                .url(this.url)
                .nombre(this.nombre)
                .fechaSubida(this.fechaSubida)
                .build();
    }
}
