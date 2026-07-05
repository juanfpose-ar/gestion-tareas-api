package com.gestortareas.api.recordatorio.service;

import com.gestortareas.api.enums.TipoRecordatorio;
import com.gestortareas.api.exceptions.BusinessValidationException;
import com.gestortareas.api.exceptions.ConflictException;
import com.gestortareas.api.exceptions.EntityNotFoundException;
import com.gestortareas.api.recordatorio.dto.RecordatorioDTO;
import com.gestortareas.api.recordatorio.entity.Recordatorio;
import com.gestortareas.api.recordatorio.repository.RecordatorioRepository;
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
public class RecordatorioServiceImplTest {

    @Mock
    private RecordatorioRepository recordatorioRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private RecordatorioServiceImpl recordatorioService;

    private Ticket ticket;
    private Recordatorio recordatorio;

    @BeforeEach
    public void setup() {
        ticket = Ticket.builder()
                .id(1L)
                .titulo("Ticket Test")
                .fechaVencimiento(LocalDateTime.now().plusDays(5))
                .build();
        recordatorio = new Recordatorio();
        recordatorio.setId(10L);
        recordatorio.setTicket(ticket);
        recordatorio.setTipo(TipoRecordatorio.PERSONALIZADO);
        recordatorio.setFechaPersonalizada(LocalDateTime.now().plusDays(2));
        recordatorio.setNotificado(false);
    }

    @Test
    public void testFindByTicketId() {
        when(recordatorioRepository.findByTicketId(1L)).thenReturn(List.of(recordatorio));

        List<RecordatorioDTO> result = recordatorioService.findByTicketId(1L);

        assertEquals(1, result.size());
        assertEquals(TipoRecordatorio.PERSONALIZADO, result.get(0).getTipo());
        assertEquals(10L, result.get(0).getId());
    }

    @Test
    public void testAddRecordatorio_Exitoso() {
        RecordatorioDTO request = RecordatorioDTO.builder()
                .tipo(TipoRecordatorio.PERSONALIZADO)
                .fechaPersonalizada(LocalDateTime.now().plusDays(2))
                .build();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(recordatorioRepository.findByTicketIdAndTipo(1L, TipoRecordatorio.PERSONALIZADO)).thenReturn(Optional.empty());
        when(recordatorioRepository.save(any(Recordatorio.class))).thenAnswer(inv -> {
            Recordatorio r = inv.getArgument(0);
            r.setId(11L);
            return r;
        });

        RecordatorioDTO result = recordatorioService.addRecordatorio(1L, request);

        assertNotNull(result);
        assertEquals(11L, result.getId());
        assertEquals(TipoRecordatorio.PERSONALIZADO, result.getTipo());
    }

    @Test
    public void testAddRecordatorio_TicketNoEncontrado() {
        RecordatorioDTO request = RecordatorioDTO.builder().tipo(TipoRecordatorio.PERSONALIZADO).build();
        when(ticketRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> recordatorioService.addRecordatorio(1L, request));
    }

    @Test
    public void testAddRecordatorio_TicketSinVencimiento() {
        ticket.setFechaVencimiento(null);
        RecordatorioDTO request = RecordatorioDTO.builder().tipo(TipoRecordatorio.PERSONALIZADO).build();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(BusinessValidationException.class, () -> recordatorioService.addRecordatorio(1L, request));
    }

    @Test
    public void testAddRecordatorio_Duplicado() {
        RecordatorioDTO request = RecordatorioDTO.builder().tipo(TipoRecordatorio.PERSONALIZADO).build();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(recordatorioRepository.findByTicketIdAndTipo(1L, TipoRecordatorio.PERSONALIZADO)).thenReturn(Optional.of(recordatorio));

        assertThrows(ConflictException.class, () -> recordatorioService.addRecordatorio(1L, request));
    }

    @Test
    public void testAddRecordatorio_PersonalizadoSinFecha() {
        RecordatorioDTO request = RecordatorioDTO.builder().tipo(TipoRecordatorio.PERSONALIZADO).build();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(recordatorioRepository.findByTicketIdAndTipo(1L, TipoRecordatorio.PERSONALIZADO)).thenReturn(Optional.empty());

        assertThrows(BusinessValidationException.class, () -> recordatorioService.addRecordatorio(1L, request));
    }

    @Test
    public void testAddRecordatorio_PersonalizadoFechaPosterior() {
        RecordatorioDTO request = RecordatorioDTO.builder()
                .tipo(TipoRecordatorio.PERSONALIZADO)
                .fechaPersonalizada(LocalDateTime.now().plusDays(10)) // Vencimiento is 5 days
                .build();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(recordatorioRepository.findByTicketIdAndTipo(1L, TipoRecordatorio.PERSONALIZADO)).thenReturn(Optional.empty());

        assertThrows(BusinessValidationException.class, () -> recordatorioService.addRecordatorio(1L, request));
    }

    @Test
    public void testMarcarNotificado_Exitoso() {
        when(recordatorioRepository.findById(10L)).thenReturn(Optional.of(recordatorio));

        RecordatorioDTO result = recordatorioService.marcarNotificado(10L);

        assertNotNull(result);
        assertTrue(result.getNotificado());
    }

    @Test
    public void testMarcarNotificado_NoEncontrado() {
        when(recordatorioRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> recordatorioService.marcarNotificado(10L));
    }

    @Test
    public void testDeleteRecordatorio_Exitoso() {
        when(recordatorioRepository.existsById(10L)).thenReturn(true);

        recordatorioService.deleteRecordatorio(10L);

        verify(recordatorioRepository, times(1)).deleteById(10L);
    }

    @Test
    public void testDeleteRecordatorio_NoEncontrado() {
        when(recordatorioRepository.existsById(10L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> recordatorioService.deleteRecordatorio(10L));
    }
}
