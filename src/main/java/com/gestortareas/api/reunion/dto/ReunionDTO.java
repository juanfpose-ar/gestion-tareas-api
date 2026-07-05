package com.gestortareas.api.reunion.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReunionDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private LocalDate fecha;
    private String horaInicio;
    private String horaFin;
    private String color;
    private Integer recordatorioMinutos;
    private Long tableroId;
    private List<Long> ticketIds;
}
