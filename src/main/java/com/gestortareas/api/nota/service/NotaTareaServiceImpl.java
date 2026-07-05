package com.gestortareas.api.nota.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gestortareas.api.exceptions.EntityNotFoundException;
import com.gestortareas.api.nota.dto.NotaTareaDTO;
import com.gestortareas.api.nota.entity.NotaTarea;
import com.gestortareas.api.nota.repository.NotaTareaRepository;
import com.gestortareas.api.ticket.entity.Ticket;
import com.gestortareas.api.ticket.repository.TicketRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class NotaTareaServiceImpl implements NotaTareaService {

    private final NotaTareaRepository notaRepository;
    private final TicketRepository ticketRepository;

    @Override
    public List<NotaTareaDTO> findByTicketId(Long ticketId) {
        return notaRepository.findByTicketIdOrderByFechaHoraDesc(ticketId)
                .stream().map(NotaTarea::toDTO).toList();
    }

    @Override
    public NotaTareaDTO addNota(Long ticketId, NotaTareaDTO request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket no encontrado: " + ticketId));
        NotaTarea nota = new NotaTarea();
        nota.setTicket(ticket);
        nota.setTexto(request.getTexto());
        return notaRepository.save(nota).toDTO();
    }

    @Override
    public NotaTareaDTO updateNota(Long notaId, NotaTareaDTO request) {
        NotaTarea nota = notaRepository.findById(notaId)
                .orElseThrow(() -> new EntityNotFoundException("Nota no encontrada: " + notaId));
        if (nota.getFechaHora().isBefore(java.time.LocalDateTime.now().minusHours(1))) {
            throw new com.gestortareas.api.exceptions.BusinessValidationException("No se puede editar una nota con más de una hora de antigüedad");
        }
        nota.setTexto(request.getTexto());
        return nota.toDTO();
    }

    @Override
    public void deleteNota(Long notaId) {
        if (!notaRepository.existsById(notaId)) {
            throw new EntityNotFoundException("Nota no encontrada: " + notaId);
        }
        notaRepository.deleteById(notaId);
    }
}
