package com.gestortareas.api.usuario.dto;

import com.gestortareas.api.enums.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class UsuarioRequest {
    @NotBlank(message = "El usuario es requerido")
    @Size(min = 3, max = 50)
    private String username;

    private String password; // Requerido al crear, opcional al editar

    @NotBlank(message = "El nombre completo es requerido")
    private String nombreCompleto;

    @NotNull(message = "El rol es requerido")
    private Rol rol;

    private boolean activo = true;

    private Set<Long> tablerosIds;
    private String email;
    private String colorAvatar;
}
