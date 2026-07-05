package com.gestortareas.api.mensajeria.dto;

import java.time.LocalDateTime;

public record ConversacionResumenDTO(
    Long id,
    String asunto,
    Long ultimoEmisorId,
    String ultimoEmisorNombre,
    String fragmentoUltimoMensaje,
    int totalMensajes,
    boolean tieneNoLeidos,
    LocalDateTime fechaUltimaActividad,
    boolean archivada,
    String nombresParticipantes,
    boolean destacada
) {}
