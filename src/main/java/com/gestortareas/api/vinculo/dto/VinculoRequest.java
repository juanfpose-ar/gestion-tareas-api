package com.gestortareas.api.vinculo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VinculoRequest {
    @NotNull(message = "El ticket de origen es obligatorio")
    private Long ticketOrigenId;

    @NotNull(message = "El ticket de destino es obligatorio")
    private Long ticketDestinoId;

    private String tipoVinculo = "RELACIONADO_CON";
}
