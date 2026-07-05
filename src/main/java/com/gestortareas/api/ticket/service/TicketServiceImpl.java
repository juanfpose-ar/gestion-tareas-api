package com.gestortareas.api.ticket.service;

import com.gestortareas.api.estado.entity.EstadoTablero;
import com.gestortareas.api.estado.repository.EstadoTableroRepository;
import com.gestortareas.api.etiqueta.dto.EtiquetaDTO;
import com.gestortareas.api.etiqueta.entity.Etiqueta;
import com.gestortareas.api.etiqueta.repository.EtiquetaRepository;
import com.gestortareas.api.exceptions.BusinessValidationException;
import com.gestortareas.api.exceptions.ConflictException;
import com.gestortareas.api.exceptions.EntityNotFoundException;
import com.gestortareas.api.adjunto.repository.AdjuntoRepository;
import com.gestortareas.api.checklist.repository.ChecklistItemRepository;
import com.gestortareas.api.nota.repository.NotaTareaRepository;
import com.gestortareas.api.recordatorio.repository.RecordatorioRepository;
import com.gestortareas.api.tablero.entity.Tablero;
import com.gestortareas.api.tablero.repository.TableroRepository;
import com.gestortareas.api.ticket.dto.TicketCardDTO;
import com.gestortareas.api.usuario.dto.UsuarioDTO;
import com.gestortareas.api.usuario.entity.Usuario;
import com.gestortareas.api.usuario.repository.UsuarioRepository;
import com.gestortareas.api.ticket.dto.TicketDTO;
import com.gestortareas.api.ticket.dto.TicketRequest;
import com.gestortareas.api.ticket.entity.Ticket;
import com.gestortareas.api.ticket.repository.TicketRepository;
import com.gestortareas.api.vinculo.service.VinculoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private static final List<String> DONE_KEYWORDS = List.of(
        "hecho", "terminado", "done", "finalizado", "completado",
        "cerrado", "listo", "resuelto", "resolved", "closed", "finished"
    );

    private static boolean esEstadoCompletado(String nombreEstado) {
        if (nombreEstado == null) return false;
        String nombre = nombreEstado.toLowerCase();
        return DONE_KEYWORDS.stream().anyMatch(nombre::contains);
    }

    private final TicketRepository ticketRepository;
    private final TableroRepository tableroRepository;
    private final EstadoTableroRepository estadoRepository;
    private final EtiquetaRepository etiquetaRepository;
    private final VinculoService vinculoService;
    private final UsuarioRepository usuarioRepository;
    private final ChecklistItemRepository checklistItemRepository;
    private final NotaTareaRepository notaTareaRepository;
    private final RecordatorioRepository recordatorioRepository;
    private final AdjuntoRepository adjuntoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TicketCardDTO> listarActivosPorTablero(Long tableroId) {
        return ticketRepository.findByTableroIdAndArchivadoFalseOrderByOrdenAscIdAsc(tableroId).stream()
                .map(this::mapToCardDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketCardDTO> listarArchivadosPorTablero(Long tableroId) {
        return ticketRepository.findByTableroIdAndArchivadoTrueOrderByFechaArchivadoDesc(tableroId).stream()
                .map(this::mapToCardDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketCardDTO> listarPorVersion(Long versionId) {
        return ticketRepository.findByVersionId(versionId).stream()
                .map(this::mapToCardDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TicketDTO obtenerPorId(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ticket no encontrado con ID: " + id));
        return mapToDTO(ticket);
    }

    @Override
    @Transactional
    public TicketDTO crearTicket(TicketRequest request, Long creadorId) {
        Tablero tablero = tableroRepository.findById(request.getTableroId())
                .orElseThrow(
                        () -> new EntityNotFoundException("Tablero no encontrado con ID: " + request.getTableroId()));

        EstadoTablero estado = estadoRepository.findById(request.getEstadoId())
                .orElseThrow(
                        () -> new EntityNotFoundException("Estado no encontrado con ID: " + request.getEstadoId()));

        if (!estado.getTablero().getId().equals(tablero.getId())) {
            throw new BusinessValidationException("El estado seleccionado no pertenece al tablero especificado");
        }

        Set<Etiqueta> etiquetas = obtenerEtiquetasPorIds(request.getEtiquetasIds());

        LocalDateTime fVenc = null;
        if (request.getFechaVencimiento() != null && !request.getFechaVencimiento().isBlank()) {
            try {
                if (request.getFechaVencimiento().contains("T")) {
                    fVenc = LocalDateTime.parse(request.getFechaVencimiento());
                } else {
                    fVenc = LocalDate.parse(request.getFechaVencimiento()).atStartOfDay();
                }
                if (!fVenc.toLocalDate().isAfter(LocalDate.now())) {
                    throw new BusinessValidationException("La fecha de vencimiento del ticket debe ser posterior al día de hoy.");
                }
            } catch (BusinessValidationException e) {
                throw e;
            } catch (Exception ignored) {
            }
        }

        int maxOrden = ticketRepository.findByEstadoIdAndArchivadoFalse(estado.getId())
                .stream().mapToInt(t -> t.getOrden() != null ? t.getOrden() : 0).max().orElse(0);

        Usuario creador = creadorId != null
                ? usuarioRepository.findById(creadorId).orElse(null)
                : null;

        Ticket ticket = Ticket.builder()
                .titulo(request.getTitulo())
                .descripcion(request.getDescripcion())
                .prioridad(request.getPrioridad())
                .tablero(tablero)
                .estado(estado)
                .archivado(false)
                .completado(request.getCompletado() != null && request.getCompletado())
                .orden(maxOrden + 1)
                .fechaVencimiento(fVenc)
                .etiquetas(etiquetas)
                .creador(creador)
                .build();

        Ticket saved = ticketRepository.save(ticket);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public TicketDTO actualizarTicket(Long id, TicketRequest request, Long modificadorId) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ticket no encontrado con ID: " + id));

        if (request.getFechaModificacion() != null && !request.getFechaModificacion().isBlank()) {
            try {
                LocalDateTime clientTs = LocalDateTime.parse(request.getFechaModificacion()).truncatedTo(ChronoUnit.SECONDS);
                LocalDateTime serverTs = ticket.getFechaModificacion() != null
                        ? ticket.getFechaModificacion().truncatedTo(ChronoUnit.SECONDS)
                        : null;
                if (serverTs != null && !serverTs.equals(clientTs)) {
                    String modificadoPor = ticket.getUltimoModificadoPor() != null
                            ? ticket.getUltimoModificadoPor().getNombreCompleto()
                            : "otro usuario";
                    throw new ConflictException(
                            "El ticket fue modificado por " + modificadoPor + " mientras lo editabas. Recargá el ticket para ver los últimos cambios.");
                }
            } catch (ConflictException e) {
                throw e;
            } catch (Exception ignored) {
            }
        }

        EstadoTablero estado = estadoRepository.findById(request.getEstadoId())
                .orElseThrow(
                        () -> new EntityNotFoundException("Estado no encontrado con ID: " + request.getEstadoId()));

        if (!ticket.getEstado().getId().equals(request.getEstadoId())) {
            LocalDateTime activeDueDate = ticket.getFechaVencimiento();
            if (request.getFechaVencimiento() != null) {
                if (request.getFechaVencimiento().isBlank()) {
                    activeDueDate = null;
                } else {
                    try {
                        if (request.getFechaVencimiento().contains("T")) {
                            activeDueDate = LocalDateTime.parse(request.getFechaVencimiento());
                        } else {
                            activeDueDate = LocalDate.parse(request.getFechaVencimiento()).atStartOfDay();
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
            if (activeDueDate == null) {
                throw new BusinessValidationException(
                        "No se puede cambiar el estado de un ticket sin fecha de vencimiento");
            }
            if (ticket.getVersion() == null) {
                throw new BusinessValidationException(
                        "No se puede cambiar el estado de un ticket sin versión asignada");
            }
        }

        boolean estadoCambiado = !ticket.getEstado().getId().equals(estado.getId());
        ticket.setTitulo(request.getTitulo());
        ticket.setDescripcion(request.getDescripcion());
        ticket.setPrioridad(request.getPrioridad());
        ticket.setEstado(estado);
        if (request.getCompletado() != null) {
            boolean nextCompletado = request.getCompletado();
            if (!nextCompletado && ticket.getVersion() != null) {
                com.gestortareas.api.version.entity.VersionEstado estadoVer = ticket.getVersion().getEstado();
                if (com.gestortareas.api.version.entity.VersionEstado.FINALIZADO.equals(estadoVer) || com.gestortareas.api.version.entity.VersionEstado.CERRADO.equals(estadoVer)) {
                    throw new BusinessValidationException("No se puede desmarcar como completado un ticket que pertenece a una versión Finalizada o Cerrada.");
                }
            }
            ticket.setCompletado(nextCompletado);
        } else if (estadoCambiado) {
            ticket.setCompletado(esEstadoCompletado(estado.getNombre()));
        }

        if (request.getFechaVencimiento() != null) {
            boolean parsedSuccessfully = false;
            LocalDateTime parsedVenc = null;
            if (request.getFechaVencimiento().isBlank()) {
                parsedSuccessfully = true;
            } else {
                try {
                    if (request.getFechaVencimiento().contains("T")) {
                        parsedVenc = LocalDateTime.parse(request.getFechaVencimiento());
                    } else {
                        parsedVenc = java.time.LocalDate.parse(request.getFechaVencimiento()).atStartOfDay();
                    }
                    parsedSuccessfully = true;
                } catch (Exception ignored) {
                }
            }

            if (parsedSuccessfully) {
                boolean changed = false;
                if (ticket.getFechaVencimiento() == null) {
                    if (parsedVenc != null) {
                        changed = true;
                    }
                } else {
                    if (parsedVenc == null || !ticket.getFechaVencimiento().equals(parsedVenc)) {
                        changed = true;
                    }
                }

                if (changed) {
                    if (parsedVenc != null && !parsedVenc.toLocalDate().isAfter(LocalDate.now())) {
                        throw new BusinessValidationException("La fecha de vencimiento del ticket debe ser posterior al día de hoy.");
                    }
                    ticket.setFechaVencimiento(parsedVenc);
                    List<com.gestortareas.api.recordatorio.entity.Recordatorio> recordatorios = recordatorioRepository
                            .findByTicketId(id);
                    if (recordatorios != null && !recordatorios.isEmpty()) {
                        recordatorioRepository.deleteAll(recordatorios);
                    }
                }
            }
        }

        if (request.getEtiquetasIds() != null) {
            ticket.setEtiquetas(obtenerEtiquetasPorIds(request.getEtiquetasIds()));
        }

        if (modificadorId != null) {
            usuarioRepository.findById(modificadorId).ifPresent(ticket::setUltimoModificadoPor);
        }

        Ticket updated = ticketRepository.save(ticket);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public TicketDTO cambiarEstado(Long id, Long nuevoEstadoId) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ticket no encontrado con ID: " + id));

        EstadoTablero nuevoEstado = estadoRepository.findById(nuevoEstadoId)
                .orElseThrow(() -> new EntityNotFoundException("Estado no encontrado con ID: " + nuevoEstadoId));

        if (!nuevoEstado.getTablero().getId().equals(ticket.getTablero().getId())) {
            throw new BusinessValidationException("El estado no pertenece al tablero del ticket");
        }

        if (ticket.getFechaVencimiento() == null) {
            throw new BusinessValidationException(
                    "No se puede cambiar el estado de un ticket sin fecha de vencimiento");
        }
        if (ticket.getVersion() == null) {
            throw new BusinessValidationException(
                    "No se puede cambiar el estado de un ticket sin versión asignada");
        }

        int maxOrden = ticketRepository.findByEstadoIdAndArchivadoFalse(nuevoEstado.getId())
                .stream().filter(t -> !t.getId().equals(id))
                .mapToInt(t -> t.getOrden() != null ? t.getOrden() : 0).max().orElse(0);

        ticket.setEstado(nuevoEstado);
        ticket.setOrden(maxOrden + 1);
        ticket.setCompletado(esEstadoCompletado(nuevoEstado.getNombre()));
        Ticket updated = ticketRepository.save(ticket);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void reordenarEnEstado(Long estadoId, List<Long> orderedIds) {
        List<Ticket> tickets = ticketRepository.findByEstadoIdAndArchivadoFalse(estadoId);
        Map<Long, Ticket> map = tickets.stream().collect(Collectors.toMap(Ticket::getId, t -> t));
        for (int i = 0; i < orderedIds.size(); i++) {
            int orden = i + 1;
            Ticket t = map.get(orderedIds.get(i));
            if (t != null) {
                t.setOrden(orden);
                ticketRepository.save(t);
            }
        }
    }

    @Override
    @Transactional
    public TicketDTO cambiarArchivado(Long id, boolean archivado) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ticket no encontrado con ID: " + id));

        ticket.setArchivado(archivado);
        if (archivado) {
            ticket.setFechaArchivado(LocalDateTime.now());
        } else {
            ticket.setFechaArchivado(null);
        }

        Ticket updated = ticketRepository.save(ticket);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public TicketDTO cambiarCompletado(Long id, boolean completado) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ticket no encontrado con ID: " + id));

        if (!completado && ticket.getVersion() != null) {
            com.gestortareas.api.version.entity.VersionEstado estadoVer = ticket.getVersion().getEstado();
            if (com.gestortareas.api.version.entity.VersionEstado.FINALIZADO.equals(estadoVer) || com.gestortareas.api.version.entity.VersionEstado.CERRADO.equals(estadoVer)) {
                throw new BusinessValidationException("No se puede desmarcar como completado un ticket que pertenece a una versión Finalizada o Cerrada.");
            }
        }

        ticket.setCompletado(completado);
        Ticket updated = ticketRepository.save(ticket);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void eliminarTicket(Long id) {
        if (!ticketRepository.existsById(id)) {
            throw new EntityNotFoundException("Ticket no encontrado con ID: " + id);
        }
        ticketRepository.deleteById(id);
    }

    @Override
    @Transactional
    public TicketDTO asignarUsuario(Long ticketId, Long usuarioId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket no encontrado con ID: " + ticketId));
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + usuarioId));
        ticket.getAsignados().add(usuario);
        ticket.getInformados().removeIf(u -> u.getId().equals(usuarioId));
        return mapToDTO(ticketRepository.save(ticket));
    }

    @Override
    @Transactional
    public TicketDTO desasignarUsuario(Long ticketId, Long usuarioId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket no encontrado con ID: " + ticketId));
        ticket.getAsignados().removeIf(u -> u.getId().equals(usuarioId));
        return mapToDTO(ticketRepository.save(ticket));
    }

    @Override
    @Transactional
    public TicketDTO asignarInformado(Long ticketId, Long usuarioId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket no encontrado con ID: " + ticketId));
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + usuarioId));
        ticket.getInformados().add(usuario);
        return mapToDTO(ticketRepository.save(ticket));
    }

    @Override
    @Transactional
    public TicketDTO desasignarInformado(Long ticketId, Long usuarioId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket no encontrado con ID: " + ticketId));
        ticket.getInformados().removeIf(u -> u.getId().equals(usuarioId));
        return mapToDTO(ticketRepository.save(ticket));
    }

    private Set<Etiqueta> obtenerEtiquetasPorIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(etiquetaRepository.findAllById(ids));
    }

    private TicketCardDTO mapToCardDTO(Ticket t) {
        Set<EtiquetaDTO> etqs = t.getEtiquetas() == null ? new HashSet<>()
                : t.getEtiquetas().stream().map(e -> EtiquetaDTO.builder()
                        .id(e.getId()).nombre(e.getNombre()).color(e.getColor()).colorHex(e.getColor())
                        .colorTexto(e.getColorTexto() != null ? e.getColorTexto() : "#ffffff").build())
                        .collect(Collectors.toSet());

        Long id = t.getId();
        List<TicketCardDTO.AsignadoCardDTO> asignadosList = t.getAsignados() == null ? List.of()
                : t.getAsignados().stream()
                        .map(u -> TicketCardDTO.AsignadoCardDTO.builder()
                                .id(u.getId()).username(u.getUsername()).nombreCompleto(u.getNombreCompleto())
                                .colorAvatar(u.getColorAvatar() != null ? u.getColorAvatar() : "#0d6efd").build())
                        .collect(Collectors.toList());

        return TicketCardDTO.builder()
                .id(id)
                .titulo(t.getTitulo())
                .descripcion(t.getDescripcion())
                .prioridad(t.getPrioridad())
                .estadoId(t.getEstado().getId())
                .estadoNombre(t.getEstado().getNombre())
                .archivado(t.isArchivado())
                .completado(t.isCompletado())
                .orden(t.getOrden())
                .fechaCreacion(t.getFechaCreacion())
                .fechaArchivado(t.getFechaArchivado())
                .fechaVencimiento(t.getFechaVencimiento())
                .etiquetas(etqs)
                .cantidadVinculos(vinculoService.obtenerVinculosPorTicket(id).size())
                .cantidadAdjuntos(adjuntoRepository.countByTicketId(id))
                .cantidadNotas(notaTareaRepository.countByTicketId(id))
                .cantidadRecordatorios(recordatorioRepository.countByTicketId(id))
                .checklistTotal(checklistItemRepository.countByTicketId(id))
                .checklistCompletados(checklistItemRepository.countByTicketIdAndCompletadoTrue(id))
                .asignados(asignadosList)
                .versionId(t.getVersion() != null ? t.getVersion().getId() : null)
                .cantidadInformados(t.getInformados() != null ? t.getInformados().size() : 0)
                .informadosIds(t.getInformados() != null
                        ? t.getInformados().stream().map(Usuario::getId).collect(Collectors.toList())
                        : List.of())
                .fechaModificacion(t.getFechaModificacion())
                .ultimoModificadoPorNombre(t.getUltimoModificadoPor() != null ? t.getUltimoModificadoPor().getNombreCompleto() : null)
                .creadorNombre(t.getCreador() != null ? t.getCreador().getNombreCompleto() : null)
                .creadorColorAvatar(t.getCreador() != null ? t.getCreador().getColorAvatar() : null)
                .build();
    }

    private TicketDTO mapToDTO(Ticket t) {
        Set<EtiquetaDTO> etqs = t.getEtiquetas() == null ? new HashSet<>()
                : t.getEtiquetas().stream().map(e -> EtiquetaDTO.builder()
                        .id(e.getId()).nombre(e.getNombre()).color(e.getColor()).colorHex(e.getColor())
                        .tableroId(e.getTablero() != null ? e.getTablero().getId() : null).build())
                        .collect(Collectors.toSet());

        List<UsuarioDTO> asignadosList = t.getAsignados() == null ? List.of()
                : t.getAsignados().stream().map(u -> UsuarioDTO.builder()
                        .id(u.getId()).username(u.getUsername()).nombreCompleto(u.getNombreCompleto())
                        .rol(u.getRol()).activo(u.isActivo()).colorAvatar(u.getColorAvatar()).build())
                        .collect(Collectors.toList());

        List<UsuarioDTO> informadosList = t.getInformados() == null ? List.of()
                : t.getInformados().stream().map(u -> UsuarioDTO.builder()
                        .id(u.getId()).username(u.getUsername()).nombreCompleto(u.getNombreCompleto())
                        .rol(u.getRol()).activo(u.isActivo()).colorAvatar(u.getColorAvatar()).build())
                        .collect(Collectors.toList());

        Usuario creador = t.getCreador();

        return TicketDTO.builder()
                .id(t.getId())
                .titulo(t.getTitulo())
                .descripcion(t.getDescripcion())
                .prioridad(t.getPrioridad())
                .tableroId(t.getTablero().getId())
                .estadoId(t.getEstado().getId())
                .estadoNombre(t.getEstado().getNombre())
                .estadoColorHex(t.getEstado().getColorHex())
                .archivado(t.isArchivado())
                .completado(t.isCompletado())
                .fechaArchivado(t.getFechaArchivado())
                .fechaVencimiento(t.getFechaVencimiento())
                .fechaCreacion(t.getFechaCreacion())
                .fechaModificacion(t.getFechaModificacion())
                .etiquetas(etqs)
                .vinculos(vinculoService.obtenerVinculosPorTicket(t.getId()))
                .asignados(asignadosList)
                .informados(informadosList)
                .versionId(t.getVersion() != null ? t.getVersion().getId() : null)
                .creadorId(creador != null ? creador.getId() : null)
                .creadorNombre(creador != null ? creador.getNombreCompleto() : null)
                .creadorColorAvatar(creador != null ? creador.getColorAvatar() : null)
                .ultimoModificadoPorNombre(t.getUltimoModificadoPor() != null ? t.getUltimoModificadoPor().getNombreCompleto() : null)
                .build();
    }
}
