package com.gestortareas.api.adjunto.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.gestortareas.api.adjunto.dto.AdjuntoDTO;
import com.gestortareas.api.adjunto.dto.EnlaceRequest;
import com.gestortareas.api.adjunto.entity.Adjunto;
import com.gestortareas.api.adjunto.repository.AdjuntoRepository;
import com.gestortareas.api.enums.TipoAdjunto;
import com.gestortareas.api.exceptions.BusinessValidationException;
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
    private final FileStorageService fileStorageService;

    @Override
    public List<AdjuntoDTO> findByTicketId(Long ticketId) {
        return adjuntoRepository.findByTicketIdOrderByFechaSubidaDesc(ticketId)
                .stream().map(Adjunto::toDTO).toList();
    }

    @Override
    public AdjuntoDTO uploadArchivo(Long ticketId, MultipartFile file) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket no encontrado: " + ticketId));

        String rutaRelativa = fileStorageService.guardar(ticketId, file);

        Adjunto adjunto = new Adjunto();
        adjunto.setTicket(ticket);
        adjunto.setTipo(TipoAdjunto.ARCHIVO);
        adjunto.setNombre(file.getOriginalFilename());
        adjunto.setRutaAlmacenamiento(rutaRelativa);
        adjunto.setContentType(file.getContentType());
        return adjuntoRepository.save(adjunto).toDTO();
    }

    @Override
    public AdjuntoDTO addEnlace(Long ticketId, EnlaceRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket no encontrado: " + ticketId));

        String url = request.getUrl().trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new BusinessValidationException("La URL debe empezar con http:// o https://");
        }

        Adjunto adjunto = new Adjunto();
        adjunto.setTicket(ticket);
        adjunto.setTipo(TipoAdjunto.ENLACE);
        adjunto.setUrl(url);
        adjunto.setNombre(request.getNombre());
        return adjuntoRepository.save(adjunto).toDTO();
    }

    @Override
    public void deleteAdjunto(Long ticketId, Long adjuntoId) {
        Adjunto adjunto = adjuntoRepository.findByIdAndTicketId(adjuntoId, ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Adjunto no encontrado: " + adjuntoId));
        if (adjunto.getTipo() == TipoAdjunto.ARCHIVO) {
            fileStorageService.eliminar(adjunto.getRutaAlmacenamiento());
        }
        adjuntoRepository.delete(adjunto);
    }

    @Override
    public ArchivoDescargable descargarArchivo(Long ticketId, Long adjuntoId) {
        Adjunto adjunto = adjuntoRepository.findByIdAndTicketId(adjuntoId, ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Adjunto no encontrado: " + adjuntoId));
        if (adjunto.getTipo() != TipoAdjunto.ARCHIVO) {
            throw new BusinessValidationException("Este adjunto no es un archivo descargable");
        }
        return new ArchivoDescargable(
                fileStorageService.cargar(adjunto.getRutaAlmacenamiento()),
                adjunto.getNombre(),
                adjunto.getContentType()
        );
    }
}
