package com.gestortareas.api.vinculo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VinculoDTO {
    private Long id;
    private Long ticketOrigenId;
    private String ticketOrigenTitulo;
    private Long ticketDestinoId;
    private String ticketDestinoTitulo;
    private String tipoVinculo;
    private LocalDateTime fechaCreacion;
}
