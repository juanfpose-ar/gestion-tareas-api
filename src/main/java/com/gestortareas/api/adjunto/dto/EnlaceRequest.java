package com.gestortareas.api.adjunto.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnlaceRequest {

    @NotBlank(message = "La url es obligatoria")
    private String url;

    private String nombre;
}
