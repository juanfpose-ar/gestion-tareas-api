package com.gestortareas.api.adjunto.dto;

import java.time.LocalDateTime;

import com.gestortareas.api.enums.TipoAdjunto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdjuntoDTO {

    private Long id;
    private TipoAdjunto tipo;
    private String url;
    private String nombre;
    private LocalDateTime fechaSubida;
}
