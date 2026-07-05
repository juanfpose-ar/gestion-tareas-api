package com.gestortareas.api.usuario.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gestortareas.api.enums.Rol;
import com.gestortareas.api.tablero.entity.Tablero;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuarios", schema = "gestortareas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Column(name = "nombre_completo", nullable = false, length = 100)
    private String nombreCompleto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

    @Column(length = 254)
    private String email;

    @Column(name = "color_avatar", length = 20)
    @Builder.Default
    private String colorAvatar = "#0d6efd";

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "usuario_tableros",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "tablero_id")
    )
    @Builder.Default
    private Set<Tablero> tablerosAsignados = new HashSet<>();
}
