package com.gestortareas.api.version.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.gestortareas.api.version.entity.VersionEstado;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VersionDTO {
    private Long id;
    private String titulo;
    private LocalDateTime fechaVencimiento;
    private Long tableroId;
    private List<Long> ticketIds;
    private VersionEstado estado;
}
