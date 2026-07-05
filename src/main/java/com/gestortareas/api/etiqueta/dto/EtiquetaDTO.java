package com.gestortareas.api.etiqueta.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtiquetaDTO {
    private Long id;
    private String nombre;
    private String color;
    private String colorHex;
    private String colorTexto;
    private Long tableroId; // Null si es global

    public String getColorHex() {
        return colorHex != null ? colorHex : color;
    }
}

