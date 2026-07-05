package com.gestortareas.api.adjunto.service;

import com.gestortareas.api.adjunto.dto.AdjuntoDTO;
import com.gestortareas.api.adjunto.dto.EnlaceRequest;
import com.gestortareas.api.adjunto.entity.Adjunto;
import com.gestortareas.api.adjunto.repository.AdjuntoRepository;
import com.gestortareas.api.enums.TipoAdjunto;
import com.gestortareas.api.exceptions.EntityNotFoundException;
import com.gestortareas.api.ticket.entity.Ticket;
import com.gestortareas.api.ticket.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdjuntoServiceImplTest {

    @Mock
    private AdjuntoRepository adjuntoRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private AdjuntoServiceImpl adjuntoService;

    private Ticket ticket;
    private Adjunto adjunto;

    @BeforeEach
    public void setup() {
        ticket = Ticket.builder().id(1L).titulo("Ticket Test").build();
        adjunto = new Adjunto(10L, ticket, TipoAdjunto.ARCHIVO, "http://test.url/test.txt", "test.txt", LocalDateTime.now());
    }

    @Test
    public void testFindByTicketId() {
        when(adjuntoRepository.findByTicketIdOrderByFechaSubidaDesc(1L)).thenReturn(List.of(adjunto));

        List<AdjuntoDTO> result = adjuntoService.findByTicketId(1L);

        assertEquals(1, result.size());
        assertEquals("test.txt", result.get(0).getNombre());
        assertEquals(TipoAdjunto.ARCHIVO, result.get(0).getTipo());
    }

    @Test
    public void testUploadArchivo_Exitoso() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(adjuntoRepository.save(any(Adjunto.class))).thenAnswer(inv -> {
            Adjunto a = inv.getArgument(0);
            a.setId(10L);
            return a;
        });

        AdjuntoDTO result = adjuntoService.uploadArchivo(1L, file);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("test.txt", result.getNombre());
        assertEquals(TipoAdjunto.ARCHIVO, result.getTipo());
        assertTrue(result.getUrl().contains("test.txt"));
    }

    @Test
    public void testUploadArchivo_TicketNoEncontrado() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        when(ticketRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> adjuntoService.uploadArchivo(1L, file));
    }

    @Test
    public void testAddEnlace_Exitoso() {
        EnlaceRequest request = new EnlaceRequest();
        request.setUrl("http://google.com");
        request.setNombre("Google");

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(adjuntoRepository.save(any(Adjunto.class))).thenAnswer(inv -> {
            Adjunto a = inv.getArgument(0);
            a.setId(11L);
            return a;
        });

        AdjuntoDTO result = adjuntoService.addEnlace(1L, request);

        assertNotNull(result);
        assertEquals(11L, result.getId());
        assertEquals("Google", result.getNombre());
        assertEquals("http://google.com", result.getUrl());
        assertEquals(TipoAdjunto.ENLACE, result.getTipo());
    }

    @Test
    public void testAddEnlace_TicketNoEncontrado() {
        EnlaceRequest request = new EnlaceRequest();
        request.setUrl("http://google.com");
        request.setNombre("Google");
        when(ticketRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> adjuntoService.addEnlace(1L, request));
    }

    @Test
    public void testDeleteAdjunto_Exitoso() {
        when(adjuntoRepository.existsById(10L)).thenReturn(true);

        adjuntoService.deleteAdjunto(10L);

        verify(adjuntoRepository, times(1)).deleteById(10L);
    }

    @Test
    public void testDeleteAdjunto_NoEncontrado() {
        when(adjuntoRepository.existsById(10L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> adjuntoService.deleteAdjunto(10L));
    }
}
