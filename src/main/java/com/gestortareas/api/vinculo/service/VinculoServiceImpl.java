package com.gestortareas.api.vinculo.service;

import com.gestortareas.api.exceptions.BusinessValidationException;
import com.gestortareas.api.exceptions.EntityNotFoundException;
import com.gestortareas.api.ticket.entity.Ticket;
import com.gestortareas.api.ticket.repository.TicketRepository;
import com.gestortareas.api.vinculo.dto.VinculoDTO;
import com.gestortareas.api.vinculo.dto.VinculoRequest;
import com.gestortareas.api.vinculo.entity.TicketVinculo;
import com.gestortareas.api.vinculo.repository.TicketVinculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VinculoServiceImpl implements VinculoService {

    private final TicketVinculoRepository vinculoRepository;
    private final TicketRepository ticketRepository;

    @Override
    @Transactional
    public VinculoDTO crearVinculo(VinculoRequest request) {
        if (request.getTicketOrigenId().equals(request.getTicketDestinoId())) {
            throw new BusinessValidationException("Un ticket no puede vincularse consigo mismo");
        }

        Ticket origen = ticketRepository.findById(request.getTicketOrigenId())
                .orElseThrow(() -> new EntityNotFoundException("Ticket origen no encontrado con ID: " + request.getTicketOrigenId()));

        Ticket destino = ticketRepository.findById(request.getTicketDestinoId())
                .orElseThrow(() -> new EntityNotFoundException("Ticket destino no encontrado con ID: " + request.getTicketDestinoId()));

        if (vinculoRepository.existsByTicketOrigenIdAndTicketDestinoIdAndTipoVinculo(
                origen.getId(), destino.getId(), request.getTipoVinculo())) {
            throw new BusinessValidationException("El vínculo entre estos tickets ya existe");
        }

        TicketVinculo vinculo = TicketVinculo.builder()
                .ticketOrigen(origen)
                .ticketDestino(destino)
                .tipoVinculo(request.getTipoVinculo())
                .build();

        TicketVinculo saved = vinculoRepository.save(vinculo);
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VinculoDTO> obtenerVinculosPorTicket(Long ticketId) {
        return vinculoRepository.findAllByTicketId(ticketId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminarVinculo(Long id) {
        if (!vinculoRepository.existsById(id)) {
            throw new EntityNotFoundException("Vínculo no encontrado con ID: " + id);
        }
        vinculoRepository.deleteById(id);
    }

    private VinculoDTO mapToDTO(TicketVinculo v) {
        return VinculoDTO.builder()
                .id(v.getId())
                .ticketOrigenId(v.getTicketOrigen().getId())
                .ticketOrigenTitulo(v.getTicketOrigen().getTitulo())
                .ticketDestinoId(v.getTicketDestino().getId())
                .ticketDestinoTitulo(v.getTicketDestino().getTitulo())
                .tipoVinculo(v.getTipoVinculo())
                .fechaCreacion(v.getFechaCreacion())
                .build();
    }
}
