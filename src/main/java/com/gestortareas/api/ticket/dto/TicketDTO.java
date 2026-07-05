package com.gestortareas.api.ticket.dto;

import com.gestortareas.api.enums.Prioridad;
import com.gestortareas.api.etiqueta.dto.EtiquetaDTO;
import com.gestortareas.api.usuario.dto.UsuarioDTO;
import com.gestortareas.api.vinculo.dto.VinculoDTO;
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
public class TicketDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private Prioridad prioridad;
    private Long tableroId;
    private Long estadoId;
    private String estadoNombre;
    private String estadoColorHex;
    private boolean archivado;
    private boolean completado;
    private LocalDateTime fechaArchivado;
    private LocalDateTime fechaVencimiento;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private Set<EtiquetaDTO> etiquetas;
    private List<VinculoDTO> vinculos;
    private List<UsuarioDTO> asignados;
    private List<UsuarioDTO> informados;
    private Long versionId;
    private Long creadorId;
    private String creadorNombre;
    private String creadorColorAvatar;
    private String ultimoModificadoPorNombre;
}
