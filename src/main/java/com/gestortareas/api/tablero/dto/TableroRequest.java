package com.gestortareas.api.tablero.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TableroRequest {
    @NotBlank
    @Size(max = 100)
    private String titulo;

    @Size(max = 500)
    private String descripcion;

    private String imagenFondoUrl;
}

