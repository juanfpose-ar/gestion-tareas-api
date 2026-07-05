package com.gestortareas.api.checklist.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChecklistItemDTO {

    private Long id;

    @NotBlank(message = "El texto es obligatorio")
    private String texto;

    private Boolean completado;

    private Short orden;
}
