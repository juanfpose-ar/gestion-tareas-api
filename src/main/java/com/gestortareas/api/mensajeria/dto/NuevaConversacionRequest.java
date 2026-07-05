package com.gestortareas.api.mensajeria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record NuevaConversacionRequest(
    @NotBlank(message = "El asunto es obligatorio")
    String asunto,

    @NotBlank(message = "El contenido del mensaje inicial es obligatorio")
    String contenido,

    @NotEmpty(message = "Debe especificar al menos un destinatario")
    List<Long> destinatarioIds
) {}
