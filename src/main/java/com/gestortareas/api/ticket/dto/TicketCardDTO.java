package com.gestortareas.api.ticket.dto;

import com.gestortareas.api.enums.Prioridad;
import com.gestortareas.api.etiqueta.dto.EtiquetaDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketCardDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private Prioridad prioridad;
    private Long estadoId;
    private String estadoNombre;
    private boolean archivado;
    private boolean completado;
    private Integer orden;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaArchivado;
    private LocalDateTime fechaVencimiento;
    private Set<EtiquetaDTO> etiquetas;
    private int cantidadVinculos;
    private int cantidadAdjuntos;
    private int cantidadNotas;
    private int cantidadRecordatorios;
    private int checklistTotal;
    private int checklistCompletados;
    private List<AsignadoCardDTO> asignados;
    private Long versionId;
    private int cantidadInformados;
    private List<Long> informadosIds;
    private LocalDateTime fechaModificacion;
    private String ultimoModificadoPorNombre;
    private String creadorNombre;
    private String creadorColorAvatar;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class AsignadoCardDTO {
        private Long id;
        private String username;
        private String nombreCompleto;
        private String colorAvatar;
    }
}
