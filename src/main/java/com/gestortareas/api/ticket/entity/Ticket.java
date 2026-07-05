package com.gestortareas.api.ticket.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gestortareas.api.enums.Prioridad;
import com.gestortareas.api.estado.entity.EstadoTablero;
import com.gestortareas.api.etiqueta.entity.Etiqueta;
import com.gestortareas.api.tablero.entity.Tablero;
import com.gestortareas.api.usuario.entity.Usuario;
import com.gestortareas.api.version.entity.Version;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tickets", schema = "gestortareas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Prioridad prioridad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tablero_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Tablero tablero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private EstadoTablero estado;

    @Column(nullable = false)
    @Builder.Default
    private boolean archivado = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean completado = false;

    @Column(name = "orden")
    @Builder.Default
    private Integer orden = 0;

    @Column(name = "fecha_archivado")
    private LocalDateTime fechaArchivado;

    @Column(name = "fecha_vencimiento")
    private LocalDateTime fechaVencimiento;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "version_id")
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Version version;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "ticket_etiquetas", joinColumns = @JoinColumn(name = "ticket_id"), inverseJoinColumns = @JoinColumn(name = "etiqueta_id"))
    @Builder.Default
    private Set<Etiqueta> etiquetas = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creador_id")
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Usuario creador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ultimo_modificado_por_id")
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Usuario ultimoModificadoPor;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "ticket_asignados", joinColumns = @JoinColumn(name = "ticket_id"), inverseJoinColumns = @JoinColumn(name = "usuario_id"))
    @Builder.Default
    private Set<Usuario> asignados = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "ticket_informados", schema = "gestortareas", joinColumns = @JoinColumn(name = "ticket_id"), inverseJoinColumns = @JoinColumn(name = "usuario_id"))
    @Builder.Default
    private Set<Usuario> informados = new HashSet<>();

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        fechaModificacion = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        fechaModificacion = LocalDateTime.now();
    }
}
