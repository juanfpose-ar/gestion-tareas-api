package com.gestortareas.api.adjunto.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.gestortareas.api.adjunto.dto.AdjuntoDTO;
import com.gestortareas.api.adjunto.dto.EnlaceRequest;
import com.gestortareas.api.adjunto.entity.Adjunto;
import com.gestortareas.api.adjunto.repository.AdjuntoRepository;
import com.gestortareas.api.enums.TipoAdjunto;
import com.gestortareas.api.exceptions.EntityNotFoundException;
import com.gestortareas.api.ticket.entity.Ticket;
import com.gestortareas.api.ticket.repository.TicketRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class AdjuntoServiceImpl implements AdjuntoService {

    private final AdjuntoRepository adjuntoRepository;
    private final TicketRepository ticketRepository;

    @Override
    public List<AdjuntoDTO> findByTicketId(Long ticketId) {
        return adjuntoRepository.findByTicketIdOrderByFechaSubidaDesc(ticketId)
                .stream().map(Adjunto::toDTO).toList();
    }

    @Override
    public AdjuntoDTO uploadArchivo(Long ticketId, MultipartFile file) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket no encontrado: " + ticketId));
        String storedUrl = "https://storage.cala.internal/adjuntos/" + UUID.randomUUID() + "/" +
                file.getOriginalFilename();
        Adjunto adjunto = new Adjunto();
        adjunto.setTicket(ticket);
        adjunto.setTipo(TipoAdjunto.ARCHIVO);
        adjunto.setUrl(storedUrl);
        adjunto.setNombre(file.getOriginalFilename());
        return adjuntoRepository.save(adjunto).toDTO();
    }

    @Override
    public AdjuntoDTO addEnlace(Long ticketId, EnlaceRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket no encontrado: " + ticketId));
        Adjunto adjunto = new Adjunto();
        adjunto.setTicket(ticket);
        adjunto.setTipo(TipoAdjunto.ENLACE);
        adjunto.setUrl(request.getUrl());
        adjunto.setNombre(request.getNombre());
        return adjuntoRepository.save(adjunto).toDTO();
    }

    @Override
    public void deleteAdjunto(Long adjuntoId) {
        if (!adjuntoRepository.existsById(adjuntoId)) {
            throw new EntityNotFoundException("Adjunto no encontrado: " + adjuntoId);
        }
        adjuntoRepository.deleteById(adjuntoId);
    }
}
