package com.gestortareas.api.recordatorio.dto;

import java.time.LocalDateTime;

import com.gestortareas.api.enums.TipoRecordatorio;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecordatorioDTO {

    private Long id;

    @NotNull(message = "El tipo de recordatorio es obligatorio")
    private TipoRecordatorio tipo;

    private LocalDateTime fechaPersonalizada;

    private Boolean notificado;
}
