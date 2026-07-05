package com.gestortareas.api.vinculo.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gestortareas.api.ticket.entity.Ticket;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_vinculos", schema = "gestortareas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketVinculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ticket_origen_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Ticket ticketOrigen;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ticket_destino_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Ticket ticketDestino;

    @Column(name = "tipo_vinculo", nullable = false, length = 50)
    private String tipoVinculo;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
}
