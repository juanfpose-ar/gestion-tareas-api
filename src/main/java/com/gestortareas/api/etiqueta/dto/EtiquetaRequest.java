package com.gestortareas.api.etiqueta.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EtiquetaRequest {
    @NotBlank(message = "El nombre de la etiqueta es obligatorio")
    private String nombre;

    private String color;

    private String colorHex;

    private String colorTexto; // "#ffffff" o "#000000"

    private Long tableroId; // Null para etiquetas globales

    public String getColor() {
        if (color != null && !color.isBlank()) {
            return color;
        }
        return colorHex;
    }
}

