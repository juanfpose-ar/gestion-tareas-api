package com.gestortareas.api.vinculo.service;

import com.gestortareas.api.exceptions.BusinessValidationException;
import com.gestortareas.api.exceptions.EntityNotFoundException;
import com.gestortareas.api.ticket.entity.Ticket;
import com.gestortareas.api.ticket.repository.TicketRepository;
import com.gestortareas.api.vinculo.dto.VinculoDTO;
import com.gestortareas.api.vinculo.dto.VinculoRequest;
import com.gestortareas.api.vinculo.entity.TicketVinculo;
import com.gestortareas.api.vinculo.repository.TicketVinculoRepository;
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
public class VinculoServiceImplTest {

    @Mock
    private TicketVinculoRepository vinculoRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private VinculoServiceImpl vinculoService;

    private Ticket ticketOrigen;
    private Ticket ticketDestino;
    private TicketVinculo vinculo;

    @BeforeEach
    public void setup() {
        ticketOrigen = Ticket.builder().id(1L).titulo("Origen").build();
        ticketDestino = Ticket.builder().id(2L).titulo("Destino").build();
        vinculo = TicketVinculo.builder()
                .id(10L)
                .ticketOrigen(ticketOrigen)
                .ticketDestino(ticketDestino)
                .tipoVinculo("BLOQUEA")
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

    @Test
    public void testCrearVinculo_Exitoso() {
        VinculoRequest request = new VinculoRequest();
        request.setTicketOrigenId(1L);
        request.setTicketDestinoId(2L);
        request.setTipoVinculo("BLOQUEA");

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticketOrigen));
        when(ticketRepository.findById(2L)).thenReturn(Optional.of(ticketDestino));
        when(vinculoRepository.existsByTicketOrigenIdAndTicketDestinoIdAndTipoVinculo(1L, 2L, "BLOQUEA")).thenReturn(false);
        when(vinculoRepository.save(any(TicketVinculo.class))).thenAnswer(inv -> {
            TicketVinculo tv = inv.getArgument(0);
            tv.setId(10L);
            return tv;
        });

        VinculoDTO result = vinculoService.crearVinculo(request);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("BLOQUEA", result.getTipoVinculo());
    }

    @Test
    public void testCrearVinculo_ConsigoMismo() {
        VinculoRequest request = new VinculoRequest();
        request.setTicketOrigenId(1L);
        request.setTicketDestinoId(1L);
        request.setTipoVinculo("BLOQUEA");

        assertThrows(BusinessValidationException.class, () -> vinculoService.crearVinculo(request));
    }

    @Test
    public void testCrearVinculo_Duplicado() {
        VinculoRequest request = new VinculoRequest();
        request.setTicketOrigenId(1L);
        request.setTicketDestinoId(2L);
        request.setTipoVinculo("BLOQUEA");

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticketOrigen));
        when(ticketRepository.findById(2L)).thenReturn(Optional.of(ticketDestino));
        when(vinculoRepository.existsByTicketOrigenIdAndTicketDestinoIdAndTipoVinculo(1L, 2L, "BLOQUEA")).thenReturn(true);

        assertThrows(BusinessValidationException.class, () -> vinculoService.crearVinculo(request));
    }

    @Test
    public void testObtenerVinculosPorTicket() {
        when(vinculoRepository.findAllByTicketId(1L)).thenReturn(List.of(vinculo));

        List<VinculoDTO> result = vinculoService.obtenerVinculosPorTicket(1L);

        assertEquals(1, result.size());
        assertEquals("BLOQUEA", result.get(0).getTipoVinculo());
    }

    @Test
    public void testEliminarVinculo_Exitoso() {
        when(vinculoRepository.existsById(10L)).thenReturn(true);

        vinculoService.eliminarVinculo(10L);

        verify(vinculoRepository, times(1)).deleteById(10L);
    }

    @Test
    public void testEliminarVinculo_NoEncontrado() {
        when(vinculoRepository.existsById(10L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> vinculoService.eliminarVinculo(10L));
    }
}
