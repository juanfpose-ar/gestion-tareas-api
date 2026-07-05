package com.gestortareas.api.version.dto;

import com.gestortareas.api.version.entity.VersionEstado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VersionRequest {
    @NotBlank(message = "El título de la versión es obligatorio")
    private String titulo;

    @NotBlank(message = "La fecha de vencimiento es obligatoria")
    private String fechaVencimiento;

    @NotNull(message = "El tablero es obligatorio")
    private Long tableroId;

    private List<Long> ticketIds;

    private VersionEstado estado;
}
