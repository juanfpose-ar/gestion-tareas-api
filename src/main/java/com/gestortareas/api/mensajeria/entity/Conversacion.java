package com.gestortareas.api.mensajeria.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conversaciones", schema = "gestortareas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String asunto;

    @Column(name = "fecha_ultima_actividad", nullable = false)
    @Builder.Default
    private LocalDateTime fechaUltimaActividad = LocalDateTime.now();

    @OneToMany(mappedBy = "conversacion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Mensaje> mensajes = new ArrayList<>();

    @OneToMany(mappedBy = "conversacion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<UsuarioConversacion> participantes = new ArrayList<>();
}
