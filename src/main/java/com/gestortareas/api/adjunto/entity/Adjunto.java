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

    /** Solo para tipo ENLACE: la URL externa. Para ARCHIVO queda null (ver toDTO). */
    @Column(length = 500)
    private String url;

    @Column(length = 255)
    private String nombre;

    @Column(nullable = false)
    private LocalDateTime fechaSubida = LocalDateTime.now();

    /** Solo para tipo ARCHIVO: ruta relativa dentro del directorio de uploads. */
    @Column(name = "ruta_almacenamiento", length = 500)
    private String rutaAlmacenamiento;

    /** Solo para tipo ARCHIVO: content-type original, usado al servir la descarga. */
    @Column(name = "content_type", length = 150)
    private String contentType;

    public AdjuntoDTO toDTO() {
        String urlFinal = this.tipo == TipoAdjunto.ARCHIVO
                ? "/api/tickets/" + this.ticket.getId() + "/adjuntos/" + this.id + "/archivo"
                : this.url;
        return AdjuntoDTO.builder()
                .id(this.id)
                .tipo(this.tipo)
                .url(urlFinal)
                .nombre(this.nombre)
                .fechaSubida(this.fechaSubida)
                .build();
    }
}
