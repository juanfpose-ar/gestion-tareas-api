package com.gestortareas.api.adjunto.service;

import com.gestortareas.api.adjunto.dto.AdjuntoDTO;
import com.gestortareas.api.adjunto.dto.EnlaceRequest;
import com.gestortareas.api.adjunto.entity.Adjunto;
import com.gestortareas.api.adjunto.repository.AdjuntoRepository;
import com.gestortareas.api.adjunto.service.AdjuntoService.ArchivoDescargable;
import com.gestortareas.api.enums.TipoAdjunto;
import com.gestortareas.api.exceptions.BusinessValidationException;
import com.gestortareas.api.exceptions.EntityNotFoundException;
import com.gestortareas.api.ticket.entity.Ticket;
import com.gestortareas.api.ticket.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdjuntoServiceImplTest {

    @Mock
    private AdjuntoRepository adjuntoRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private AdjuntoServiceImpl adjuntoService;

    private Ticket ticket;
    private Adjunto adjunto;

    @BeforeEach
    public void setup() {
        ticket = Ticket.builder().id(1L).titulo("Ticket Test").build();
        adjunto = new Adjunto(10L, ticket, TipoAdjunto.ARCHIVO, null, "test.txt", LocalDateTime.now(), "1/uuid_test.txt", "text/plain");
    }

    @Test
    public void testFindByTicketId() {
        when(adjuntoRepository.findByTicketIdOrderByFechaSubidaDesc(1L)).thenReturn(List.of(adjunto));

        List<AdjuntoDTO> result = adjuntoService.findByTicketId(1L);

        assertEquals(1, result.size());
        assertEquals("test.txt", result.get(0).getNombre());
        assertEquals(TipoAdjunto.ARCHIVO, result.get(0).getTipo());
        assertEquals("/api/tickets/1/adjuntos/10/archivo", result.get(0).getUrl());
    }

    @Test
    public void testUploadArchivo_Exitoso() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(fileStorageService.guardar(eq(1L), any(MockMultipartFile.class))).thenReturn("1/uuid_test.txt");
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
        assertEquals("/api/tickets/1/adjuntos/10/archivo", result.getUrl());
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
    public void testAddEnlace_EsquemaInvalido() {
        EnlaceRequest request = new EnlaceRequest();
        request.setUrl("javascript:alert(1)");
        request.setNombre("Malicioso");

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(BusinessValidationException.class, () -> adjuntoService.addEnlace(1L, request));
    }

    @Test
    public void testDeleteAdjunto_Exitoso() {
        when(adjuntoRepository.findByIdAndTicketId(10L, 1L)).thenReturn(Optional.of(adjunto));

        adjuntoService.deleteAdjunto(1L, 10L);

        verify(fileStorageService, times(1)).eliminar("1/uuid_test.txt");
        verify(adjuntoRepository, times(1)).delete(adjunto);
    }

    @Test
    public void testDeleteAdjunto_NoEncontrado() {
        when(adjuntoRepository.findByIdAndTicketId(10L, 1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> adjuntoService.deleteAdjunto(1L, 10L));
    }

    @Test
    public void testDescargarArchivo_Exitoso() {
        Resource resource = mock(Resource.class);
        when(adjuntoRepository.findByIdAndTicketId(10L, 1L)).thenReturn(Optional.of(adjunto));
        when(fileStorageService.cargar("1/uuid_test.txt")).thenReturn(resource);

        ArchivoDescargable result = adjuntoService.descargarArchivo(1L, 10L);

        assertEquals(resource, result.resource());
        assertEquals("test.txt", result.nombre());
        assertEquals("text/plain", result.contentType());
    }

    @Test
    public void testDescargarArchivo_NoEsArchivo() {
        Adjunto enlace = new Adjunto(12L, ticket, TipoAdjunto.ENLACE, "http://google.com", "Google", LocalDateTime.now(), null, null);
        when(adjuntoRepository.findByIdAndTicketId(12L, 1L)).thenReturn(Optional.of(enlace));

        assertThrows(BusinessValidationException.class, () -> adjuntoService.descargarArchivo(1L, 12L));
    }
}
