package com.gestortareas.api.mensajeria.dto;

import java.time.LocalDateTime;

public record MensajeDTO(
    Long id,
    Long emisorId,
    String emisorNombre,
    String contenido,
    LocalDateTime fechaEnvio
) {}
