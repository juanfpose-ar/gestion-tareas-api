package com.gestortareas.api.estado.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EstadoTableroRequest {
    @NotBlank(message = "El nombre del estado es obligatorio")
    private String nombre;

    @NotNull(message = "El orden es obligatorio")
    private Integer orden;

    private String colorHex;

    private Long tableroId;
}
