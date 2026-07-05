package com.gestortareas.api.ticket.dto;

import com.gestortareas.api.enums.Prioridad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class TicketRequest {
    @NotBlank(message = "El título del ticket es obligatorio")
    @Size(max = 150)
    private String titulo;

    private String descripcion;

    @NotNull(message = "La prioridad es obligatoria")
    private Prioridad prioridad = Prioridad.MEDIA;

    private Long tableroId;

    @NotNull(message = "El estado es obligatorio")
    private Long estadoId;

    private Set<Long> etiquetasIds;

    private String fechaVencimiento;

    private String fechaModificacion;

    private Boolean completado;
}
