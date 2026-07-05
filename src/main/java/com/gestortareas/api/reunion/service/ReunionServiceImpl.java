package com.gestortareas.api.reunion.service;

import com.gestortareas.api.reunion.dto.ReunionDTO;
import com.gestortareas.api.reunion.dto.ReunionRequest;
import com.gestortareas.api.reunion.entity.Reunion;
import com.gestortareas.api.reunion.repository.ReunionRepository;
import com.gestortareas.api.tablero.entity.Tablero;
import com.gestortareas.api.tablero.repository.TableroRepository;
import com.gestortareas.api.ticket.entity.Ticket;
import com.gestortareas.api.ticket.repository.TicketRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReunionServiceImpl implements ReunionService {

    private final ReunionRepository reunionRepository;
    private final TableroRepository tableroRepository;
    private final TicketRepository ticketRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ReunionDTO> findByTableroId(Long tableroId) {
        return reunionRepository.findByTableroIdOrderByFechaAscHoraInicioAsc(tableroId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReunionDTO crear(ReunionRequest request) {
        Tablero tablero = tableroRepository.findById(request.getTableroId())
                .orElseThrow(() -> new EntityNotFoundException("Tablero no encontrado"));

        Reunion reunion = Reunion.builder()
                .titulo(request.getTitulo())
                .descripcion(request.getDescripcion())
                .fecha(request.getFecha())
                .horaInicio(request.getHoraInicio())
                .horaFin(request.getHoraFin())
                .color(request.getColor())
                .recordatorioMinutos(request.getRecordatorioMinutos())
                .tablero(tablero)
                .tickets(resolveTickets(request.getTicketIds()))
                .build();

        return mapToDTO(reunionRepository.save(reunion));
    }

    @Override
    @Transactional
    public ReunionDTO actualizar(Long id, ReunionRequest request) {
        Reunion reunion = reunionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reunión no encontrada"));

        reunion.setTitulo(request.getTitulo());
        reunion.setDescripcion(request.getDescripcion());
        reunion.setFecha(request.getFecha());
        reunion.setHoraInicio(request.getHoraInicio());
        reunion.setHoraFin(request.getHoraFin());
        reunion.setColor(request.getColor());
        reunion.setRecordatorioMinutos(request.getRecordatorioMinutos());
        reunion.setTickets(resolveTickets(request.getTicketIds()));

        return mapToDTO(reunionRepository.save(reunion));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!reunionRepository.existsById(id)) {
            throw new EntityNotFoundException("Reunión no encontrada");
        }
        reunionRepository.deleteById(id);
    }

    private List<Ticket> resolveTickets(List<Long> ticketIds) {
        if (ticketIds == null || ticketIds.isEmpty()) return new ArrayList<>();
        return ticketRepository.findAllById(ticketIds);
    }

    private ReunionDTO mapToDTO(Reunion r) {
        List<Long> ticketIds = r.getTickets() == null ? new ArrayList<>()
                : r.getTickets().stream().map(Ticket::getId).collect(Collectors.toList());

        return ReunionDTO.builder()
                .id(r.getId())
                .titulo(r.getTitulo())
                .descripcion(r.getDescripcion())
                .fecha(r.getFecha())
                .horaInicio(r.getHoraInicio())
                .horaFin(r.getHoraFin())
                .color(r.getColor())
                .recordatorioMinutos(r.getRecordatorioMinutos())
                .tableroId(r.getTablero().getId())
                .ticketIds(ticketIds)
                .build();
    }
}
