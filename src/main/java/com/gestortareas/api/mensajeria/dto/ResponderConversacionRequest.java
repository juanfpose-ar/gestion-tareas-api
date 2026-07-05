package com.gestortareas.api.mensajeria.dto;

import jakarta.validation.constraints.NotBlank;

public record ResponderConversacionRequest(
    @NotBlank(message = "El contenido del mensaje es obligatorio")
    String contenido
) {}
