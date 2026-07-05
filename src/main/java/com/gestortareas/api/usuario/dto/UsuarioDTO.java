package com.gestortareas.api.usuario.dto;

import com.gestortareas.api.enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioDTO {
    private Long id;
    private String username;
    private String nombreCompleto;
    private Rol rol;
    private boolean activo;
    private Set<Long> tablerosIds;
    private String email;
    private String colorAvatar;
}
