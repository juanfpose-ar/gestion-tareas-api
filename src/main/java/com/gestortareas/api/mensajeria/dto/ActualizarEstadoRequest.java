package com.gestortareas.api.mensajeria.dto;

public record ActualizarEstadoRequest(
    Boolean archivada,
    Boolean eliminada
) {}
