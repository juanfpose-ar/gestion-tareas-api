package com.gestortareas.api.recordatorio.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.gestortareas.api.enums.TipoRecordatorio;
import com.gestortareas.api.exceptions.BusinessValidationException;
import com.gestortareas.api.exceptions.ConflictException;
import com.gestortareas.api.exceptions.EntityNotFoundException;
import com.gestortareas.api.recordatorio.dto.RecordatorioDTO;
import com.gestortareas.api.recordatorio.entity.Recordatorio;
import com.gestortareas.api.recordatorio.repository.RecordatorioRepository;
import com.gestortareas.api.ticket.entity.Ticket;
import com.gestortareas.api.ticket.repository.TicketRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class RecordatorioServiceImpl implements RecordatorioService {

    private final RecordatorioRepository recordatorioRepository;
    private final TicketRepository ticketRepository;

    @Override
    public List<RecordatorioDTO> findByTicketId(Long ticketId) {
        return recordatorioRepository.findByTicketId(ticketId)
                .stream().map(Recordatorio::toDTO).toList();
    }

    @Override
    public RecordatorioDTO addRecordatorio(Long ticketId, RecordatorioDTO request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket no encontrado: " + ticketId));

        if (ticket.getFechaVencimiento() == null) {
            throw new BusinessValidationException(
                    "El ticket no tiene fechaVencimiento; no se puede crear un recordatorio");
        }

        if (recordatorioRepository.findByTicketIdAndTipo(ticketId, request.getTipo()).isPresent()) {
            throw new ConflictException("Ya existe un recordatorio de tipo '" +
                    request.getTipo().toJson() + "' para este ticket");
        }

        if (TipoRecordatorio.PERSONALIZADO == request.getTipo()) {
            LocalDateTime fechaPersonalizada = request.getFechaPersonalizada();
            if (fechaPersonalizada == null) {
                throw new BusinessValidationException(
                        "Se requiere fechaPersonalizada para tipo personalizado");
            }
            LocalDateTime limiteFecha = ticket.getFechaVencimiento();
            if (!fechaPersonalizada.isBefore(limiteFecha)) {
                throw new BusinessValidationException(
                        "La fechaPersonalizada debe ser anterior a la fechaVencimiento del ticket");
            }
        }

        Recordatorio r = new Recordatorio();
        r.setTicket(ticket);
        r.setTipo(request.getTipo());
        r.setFechaPersonalizada(request.getFechaPersonalizada());
        r.setNotificado(false);
        return recordatorioRepository.save(r).toDTO();
    }

    @Override
    public RecordatorioDTO marcarNotificado(Long recordatorioId) {
        Recordatorio r = recordatorioRepository.findById(recordatorioId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Recordatorio no encontrado: " + recordatorioId));
        r.setNotificado(true);
        return r.toDTO();
    }

    @Override
    public void deleteRecordatorio(Long recordatorioId) {
        if (!recordatorioRepository.existsById(recordatorioId)) {
            throw new EntityNotFoundException("Recordatorio no encontrado: " + recordatorioId);
        }
        recordatorioRepository.deleteById(recordatorioId);
    }
}
