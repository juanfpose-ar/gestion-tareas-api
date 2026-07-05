package com.gestortareas.api.ticket.service;

import com.gestortareas.api.ticket.dto.TicketCardDTO;
import com.gestortareas.api.ticket.dto.TicketDTO;
import com.gestortareas.api.ticket.dto.TicketRequest;

import java.util.List;

public interface TicketService {
    List<TicketCardDTO> listarActivosPorTablero(Long tableroId);
    List<TicketCardDTO> listarArchivadosPorTablero(Long tableroId);
    List<TicketCardDTO> listarPorVersion(Long versionId);
    TicketDTO obtenerPorId(Long id);
    TicketDTO crearTicket(TicketRequest request, Long creadorId);
    TicketDTO actualizarTicket(Long id, TicketRequest request, Long modificadorId);
    TicketDTO cambiarEstado(Long id, Long nuevoEstadoId);
    TicketDTO cambiarArchivado(Long id, boolean archivado);
    TicketDTO cambiarCompletado(Long id, boolean completado);
    void eliminarTicket(Long id);
    TicketDTO asignarUsuario(Long ticketId, Long usuarioId);
    TicketDTO desasignarUsuario(Long ticketId, Long usuarioId);
    TicketDTO asignarInformado(Long ticketId, Long usuarioId);
    TicketDTO desasignarInformado(Long ticketId, Long usuarioId);
    void reordenarEnEstado(Long estadoId, List<Long> orderedIds);
}
