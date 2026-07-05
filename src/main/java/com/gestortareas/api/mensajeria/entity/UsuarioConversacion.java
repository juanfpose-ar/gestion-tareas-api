package com.gestortareas.api.mensajeria.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gestortareas.api.usuario.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "usuario_conversacion", 
    schema = "gestortareas",
    uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "conversacion_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioConversacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversacion_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Conversacion conversacion;

    @Column(nullable = false)
    @Builder.Default
    private boolean leida = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean archivada = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean eliminada = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean destacada = false;
}
