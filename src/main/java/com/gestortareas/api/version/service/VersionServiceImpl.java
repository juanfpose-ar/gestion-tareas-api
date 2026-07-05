package com.gestortareas.api.version.service;

import com.gestortareas.api.exceptions.BusinessValidationException;
import com.gestortareas.api.tablero.entity.Tablero;
import com.gestortareas.api.tablero.repository.TableroRepository;
import com.gestortareas.api.ticket.entity.Ticket;
import com.gestortareas.api.ticket.repository.TicketRepository;
import com.gestortareas.api.version.dto.VersionDTO;
import com.gestortareas.api.version.dto.VersionRequest;
import com.gestortareas.api.version.entity.Version;
import com.gestortareas.api.version.entity.VersionEstado;
import com.gestortareas.api.version.repository.VersionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VersionServiceImpl implements VersionService {

    @Autowired
    private VersionRepository versionRepository;

    @Autowired
    private TableroRepository tableroRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Override
    @Transactional(readOnly = true)
    public List<VersionDTO> listarPorTablero(Long tableroId) {
        return versionRepository.findByTableroIdOrderByFechaVencimientoAsc(tableroId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VersionDTO crearVersion(VersionRequest request) {
        Tablero tablero = tableroRepository.findById(request.getTableroId())
                .orElseThrow(
                        () -> new EntityNotFoundException("Tablero no encontrado con ID: " + request.getTableroId()));

        LocalDateTime fVenc = parseFecha(request.getFechaVencimiento());
        if (!fVenc.toLocalDate().isAfter(java.time.LocalDate.now())) {
            throw new BusinessValidationException("La fecha de vencimiento debe ser posterior al día de hoy.");
        }

        VersionEstado nuevoEstado = request.getEstado() != null ? request.getEstado() : VersionEstado.POR_HACER;
        if (VersionEstado.EN_CURSO.equals(nuevoEstado) &&
                versionRepository.existsByTableroIdAndEstado(request.getTableroId(), VersionEstado.EN_CURSO)) {
            throw new BusinessValidationException("Ya existe una versión 'En curso' en este tablero. Solo puede haber una.");
        }
        validarNuevosTicketsVinculables(request.getTicketIds(), List.of(), nuevoEstado, null);

        Version version = Version.builder()
                .titulo(request.getTitulo())
                .fechaVencimiento(fVenc)
                .tablero(tablero)
                .estado(nuevoEstado)
                .build();

        Version saved = versionRepository.save(version);

        if (request.getTicketIds() != null) {
            for (Long ticketId : request.getTicketIds()) {
                Ticket ticket = ticketRepository.findById(ticketId)
                        .orElseThrow(() -> new EntityNotFoundException("Ticket no encontrado con ID: " + ticketId));
                ticket.setVersion(saved);
                ticketRepository.save(ticket);
            }
        }

        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public VersionDTO actualizarVersion(Long id, VersionRequest request) {
        Version version = versionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Versión no encontrada con ID: " + id));

        VersionEstado estadoOriginal = version.getEstado();
        LocalDateTime fVenc = parseFecha(request.getFechaVencimiento());
        java.time.LocalDate nuevaFechaDate = fVenc.toLocalDate();
        java.time.LocalDate fechaActualDate = version.getFechaVencimiento() != null ? version.getFechaVencimiento().toLocalDate() : null;
        if (!nuevaFechaDate.equals(fechaActualDate) && nuevaFechaDate.isBefore(java.time.LocalDate.now())) {
            throw new BusinessValidationException("La fecha de vencimiento no puede ser anterior a hoy.");
        }
        version.setTitulo(request.getTitulo());
        version.setFechaVencimiento(fVenc);
        if (request.getEstado() != null) {
            VersionEstado nuevoEstado = request.getEstado();
            VersionEstado estadoActual = version.getEstado();

            if (VersionEstado.POR_HACER.equals(nuevoEstado) &&
                    (VersionEstado.CERRADO.equals(estadoActual) || VersionEstado.FINALIZADO.equals(estadoActual))) {
                throw new BusinessValidationException("No se puede revertir una versión "
                        + (VersionEstado.CERRADO.equals(estadoActual) ? "Cerrada" : "Finalizada")
                        + " al estado Por hacer.");
            }
            if (VersionEstado.EN_CURSO.equals(nuevoEstado) &&
                    !VersionEstado.EN_CURSO.equals(estadoActual) &&
                    versionRepository.existsByTableroIdAndEstadoAndIdNot(version.getTablero().getId(), VersionEstado.EN_CURSO, id)) {
                throw new BusinessValidationException("Ya existe una versión 'En curso' en este tablero. Solo puede haber una.");
            }
            if ((VersionEstado.FINALIZADO.equals(nuevoEstado) || VersionEstado.CERRADO.equals(nuevoEstado)) &&
                    !nuevoEstado.equals(estadoActual)) {
                List<Ticket> tickets = ticketRepository.findByVersionId(id);
                boolean todosCompletados = tickets.stream().allMatch(Ticket::isCompletado);
                if (!todosCompletados) {
                    throw new BusinessValidationException("No se puede pasar la versión a "
                            + (VersionEstado.FINALIZADO.equals(nuevoEstado) ? "Finalizado" : "Cerrado")
                            + " porque tiene tickets pendientes de completar.");
                }
            }
            if (VersionEstado.EN_CURSO.equals(estadoActual) && VersionEstado.CERRADO.equals(nuevoEstado)) {
                throw new BusinessValidationException("No se puede pasar directamente de En curso a Cerrado.");
            }
            version.setEstado(nuevoEstado);
        }

        Version saved = versionRepository.save(version);

        // Tickets vinculados actuales en la BD
        List<Ticket> vinculadosBd = ticketRepository.findByVersionId(id);
        Set<Long> requestTicketIds = request.getTicketIds() != null ? new HashSet<>(request.getTicketIds())
                : new HashSet<>();

        if (VersionEstado.CERRADO.equals(estadoOriginal)) {
            VersionEstado targetEstado = request.getEstado() != null ? request.getEstado() : estadoOriginal;
            if (VersionEstado.CERRADO.equals(targetEstado)) {
                Set<Long> dbTicketIds = vinculadosBd.stream().map(Ticket::getId).collect(Collectors.toSet());
                if (!dbTicketIds.equals(requestTicketIds)) {
                    throw new BusinessValidationException("No se pueden modificar los tickets vinculados a una versión Cerrada.");
                }
            }
        }

        validarNuevosTicketsVinculables(new java.util.ArrayList<>(requestTicketIds), vinculadosBd, saved.getEstado(), id);

        // 1. Blanquear tickets desvinculados
        for (Ticket ticket : vinculadosBd) {
            if (!requestTicketIds.contains(ticket.getId())) {
                ticket.setVersion(null);
                ticketRepository.save(ticket);
            }
        }

        // 2. Vincular y actualizar los solicitados
        for (Long ticketId : requestTicketIds) {
            Ticket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new EntityNotFoundException("Ticket no encontrado con ID: " + ticketId));
            ticket.setVersion(saved);
            ticketRepository.save(ticket);
        }

        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public void eliminarVersion(Long id) {
        Version version = versionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Versión no encontrada con ID: " + id));

        List<Ticket> vinculados = ticketRepository.findByVersionId(id);
        for (Ticket ticket : vinculados) {
            ticket.setVersion(null);
            ticketRepository.save(ticket);
        }

        versionRepository.delete(version);
    }

    /**
     * Valida que los tickets nuevos (no previamente vinculados) cumplan las reglas
     * de vinculación según el estado de la versión destino.
     */
    private void validarNuevosTicketsVinculables(List<Long> ticketIds, List<Ticket> vinculadosBd,
                                                  VersionEstado nuevoEstado, Long versionId) {
        if (ticketIds == null || ticketIds.isEmpty()) return;
        Set<Long> yaVinculadosIds = vinculadosBd.stream().map(Ticket::getId).collect(Collectors.toSet());

        for (Long ticketId : ticketIds) {
            if (yaVinculadosIds.contains(ticketId)) continue;

            Ticket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new EntityNotFoundException("Ticket no encontrado con ID: " + ticketId));

            switch (nuevoEstado) {
                case FINALIZADO:
                case CERRADO:
                    if (!ticket.isCompletado()) {
                        throw new BusinessValidationException("El ticket #" + ticketId
                                + " no está completado y no puede vincularse a una versión "
                                + (VersionEstado.FINALIZADO.equals(nuevoEstado) ? "Finalizada" : "Cerrada") + ".");
                    }
                    if (ticket.getVersion() != null && !ticket.getVersion().getId().equals(versionId)) {
                        throw new BusinessValidationException("El ticket #" + ticketId
                                + " ya pertenece a otra versión.");
                    }
                    break;
                case EN_CURSO:
                    if (ticket.getVersion() != null && !ticket.getVersion().getId().equals(versionId)) {
                        VersionEstado estadoVer = ticket.getVersion().getEstado();
                        if (!VersionEstado.POR_HACER.equals(estadoVer)) {
                            throw new BusinessValidationException("El ticket #" + ticketId
                                    + " pertenece a una versión " + estadoVer.name()
                                    + ". Para vincularlo a una versión En curso, debe no tener versión asignada o provenir de una versión Por hacer.");
                        }
                    }
                    break;
                case POR_HACER:
                    if (ticket.getVersion() != null && !ticket.getVersion().getId().equals(versionId)) {
                        VersionEstado estadoVer = ticket.getVersion().getEstado();
                        if (!VersionEstado.EN_CURSO.equals(estadoVer)) {
                            throw new BusinessValidationException("El ticket #" + ticketId
                                    + " pertenece a una versión " + estadoVer.name()
                                    + ". Para vincularlo a una versión Por hacer, debe no tener versión asignada o provenir de una versión En curso.");
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private LocalDateTime parseFecha(String fechaStr) {
        if (fechaStr == null || fechaStr.isBlank()) {
            throw new BusinessValidationException("La fecha de vencimiento es obligatoria");
        }
        try {
            if (fechaStr.contains("T")) {
                return LocalDateTime.parse(fechaStr);
            } else {
                return java.time.LocalDate.parse(fechaStr).atStartOfDay();
            }
        } catch (Exception e) {
            throw new BusinessValidationException("Formato de fecha inválido: " + fechaStr);
        }
    }

    private VersionDTO mapToDTO(Version v) {
        List<Long> ticketIds = ticketRepository.findByVersionId(v.getId()).stream()
                .map(Ticket::getId)
                .collect(Collectors.toList());

        return VersionDTO.builder()
                .id(v.getId())
                .titulo(v.getTitulo())
                .fechaVencimiento(v.getFechaVencimiento())
                .tableroId(v.getTablero().getId())
                .ticketIds(ticketIds)
                .estado(v.getEstado())
                .build();
    }
}
