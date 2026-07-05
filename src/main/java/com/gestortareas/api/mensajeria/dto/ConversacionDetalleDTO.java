package com.gestortareas.api.mensajeria.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ConversacionDetalleDTO(
    Long id,
    String asunto,
    LocalDateTime fechaUltimaActividad,
    List<MensajeDTO> mensajes,
    List<Long> participanteIds
) {}
