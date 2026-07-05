package com.gestortareas.api.nota.service;

import com.gestortareas.api.exceptions.BusinessValidationException;
import com.gestortareas.api.exceptions.EntityNotFoundException;
import com.gestortareas.api.nota.dto.NotaTareaDTO;
import com.gestortareas.api.nota.entity.NotaTarea;
import com.gestortareas.api.nota.repository.NotaTareaRepository;
import com.gestortareas.api.ticket.entity.Ticket;
import com.gestortareas.api.ticket.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotaTareaServiceImplTest {

    @Mock
    private NotaTareaRepository notaRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private NotaTareaServiceImpl notaService;

    private Ticket ticket;
    private NotaTarea nota;

    @BeforeEach
    public void setup() {
        ticket = Ticket.builder().id(1L).titulo("Ticket Test").build();
        nota = new NotaTarea();
        nota.setId(10L);
        nota.setTicket(ticket);
        nota.setTexto("Test note");
        nota.setFechaHora(LocalDateTime.now());
    }

    @Test
    public void testFindByTicketId() {
        when(notaRepository.findByTicketIdOrderByFechaHoraDesc(1L)).thenReturn(List.of(nota));

        List<NotaTareaDTO> result = notaService.findByTicketId(1L);

        assertEquals(1, result.size());
        assertEquals("Test note", result.get(0).getTexto());
        assertEquals(10L, result.get(0).getId());
    }

    @Test
    public void testAddNota_Exitoso() {
        NotaTareaDTO request = NotaTareaDTO.builder().texto("New note").build();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(notaRepository.save(any(NotaTarea.class))).thenAnswer(inv -> {
            NotaTarea n = inv.getArgument(0);
            n.setId(11L);
            return n;
        });

        NotaTareaDTO result = notaService.addNota(1L, request);

        assertNotNull(result);
        assertEquals(11L, result.getId());
        assertEquals("New note", result.getTexto());
    }

    @Test
    public void testAddNota_TicketNoEncontrado() {
        NotaTareaDTO request = NotaTareaDTO.builder().texto("New note").build();
        when(ticketRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> notaService.addNota(1L, request));
    }

    @Test
    public void testUpdateNota_Exitoso() {
        NotaTareaDTO request = NotaTareaDTO.builder().texto("Updated text").build();

        when(notaRepository.findById(10L)).thenReturn(Optional.of(nota));

        NotaTareaDTO result = notaService.updateNota(10L, request);

        assertNotNull(result);
        assertEquals("Updated text", result.getTexto());
    }

    @Test
    public void testUpdateNota_AntiguaFalla() {
        // Set date to 2 hours ago
        nota.setFechaHora(LocalDateTime.now().minusHours(2));
        NotaTareaDTO request = NotaTareaDTO.builder().texto("Updated text").build();

        when(notaRepository.findById(10L)).thenReturn(Optional.of(nota));

        assertThrows(BusinessValidationException.class, () -> notaService.updateNota(10L, request));
    }

    @Test
    public void testUpdateNota_NoEncontrada() {
        NotaTareaDTO request = NotaTareaDTO.builder().texto("Updated text").build();
        when(notaRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> notaService.updateNota(10L, request));
    }

    @Test
    public void testDeleteNota_Exitoso() {
        when(notaRepository.existsById(10L)).thenReturn(true);

        notaService.deleteNota(10L);

        verify(notaRepository, times(1)).deleteById(10L);
    }

    @Test
    public void testDeleteNota_NoEncontrada() {
        when(notaRepository.existsById(10L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> notaService.deleteNota(10L));
    }
}
