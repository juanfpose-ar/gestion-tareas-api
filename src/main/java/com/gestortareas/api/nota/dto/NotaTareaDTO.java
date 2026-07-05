package com.gestortareas.api.nota.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotaTareaDTO {

    private Long id;

    @NotBlank(message = "El texto es obligatorio")
    private String texto;

    private LocalDateTime fechaHora;
}
