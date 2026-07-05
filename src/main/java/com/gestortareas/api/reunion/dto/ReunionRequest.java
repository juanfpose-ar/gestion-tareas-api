package com.gestortareas.api.reunion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ReunionRequest {

    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    private String descripcion;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    private String horaInicio;

    private String horaFin;

    private String color;

    private Integer recordatorioMinutos;

    @NotNull(message = "El tablero es obligatorio")
    private Long tableroId;

    private List<Long> ticketIds;
}
