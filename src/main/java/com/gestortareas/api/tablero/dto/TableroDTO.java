package com.gestortareas.api.tablero.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableroDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private String imagenFondoUrl;
    private LocalDateTime fechaCreacion;
    private boolean archivado;
}

