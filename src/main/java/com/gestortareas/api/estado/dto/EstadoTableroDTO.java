package com.gestortareas.api.estado.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoTableroDTO {
    private Long id;
    private String nombre;
    private Integer orden;
    private String colorHex;
    private Long tableroId;
}
