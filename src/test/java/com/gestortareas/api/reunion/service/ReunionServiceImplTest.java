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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReunionServiceImplTest {

    @Mock
    private ReunionRepository reunionRepository;

    @Mock
    private TableroRepository tableroRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private ReunionServiceImpl reunionService;

    private Tablero tablero;
    private Reunion reunion;
    private Ticket ticket;

    @BeforeEach
    public void setup() {
        tablero = Tablero.builder().id(1L).titulo("Tablero Test").build();
        ticket = Ticket.builder().id(100L).titulo("Ticket 1").build();
        reunion = Reunion.builder()
                .id(10L)
                .titulo("Daily Meeting")
                .descripcion("Daily Standup")
                .fecha(LocalDate.now())
                .horaInicio("09:00")
                .horaFin("09:15")
                .color("#00ff00")
                .recordatorioMinutos(15)
                .tablero(tablero)
                .tickets(new ArrayList<>(Collections.singletonList(ticket)))
                .build();
    }

    @Test
    public void testFindByTableroId() {
        when(reunionRepository.findByTableroIdOrderByFechaAscHoraInicioAsc(1L)).thenReturn(List.of(reunion));

        List<ReunionDTO> result = reunionService.findByTableroId(1L);

        assertEquals(1, result.size());
        assertEquals("Daily Meeting", result.get(0).getTitulo());
        assertEquals(10L, result.get(0).getId());
    }

    @Test
    public void testCrear_Exitoso() {
        ReunionRequest request = new ReunionRequest();
        request.setTitulo("Sprint Planning");
        request.setDescripcion("Plan Q3");
        request.setFecha(LocalDate.now());
        request.setHoraInicio("10:00");
        request.setHoraFin("11:00");
        request.setColor("#0000ff");
        request.setRecordatorioMinutos(30);
        request.setTableroId(1L);
        request.setTicketIds(List.of(100L));

        when(tableroRepository.findById(1L)).thenReturn(Optional.of(tablero));
        when(ticketRepository.findAllById(List.of(100L))).thenReturn(List.of(ticket));
        when(reunionRepository.save(any(Reunion.class))).thenAnswer(inv -> {
            Reunion r = inv.getArgument(0);
            r.setId(11L);
            return r;
        });

        ReunionDTO result = reunionService.crear(request);

        assertNotNull(result);
        assertEquals(11L, result.getId());
        assertEquals("Sprint Planning", result.getTitulo());
        assertTrue(result.getTicketIds().contains(100L));
    }

    @Test
    public void testCrear_TableroNoEncontrado() {
        ReunionRequest request = new ReunionRequest();
        request.setTitulo("Sprint Planning");
        request.setDescripcion("Plan Q3");
        request.setFecha(LocalDate.now());
        request.setHoraInicio("10:00");
        request.setHoraFin("11:00");
        request.setColor("#0000ff");
        request.setRecordatorioMinutos(30);
        request.setTableroId(1L);

        when(tableroRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> reunionService.crear(request));
    }

    @Test
    public void testActualizar_Exitoso() {
        ReunionRequest request = new ReunionRequest();
        request.setTitulo("Daily Mod");
        request.setDescripcion("New desc");
        request.setFecha(LocalDate.now());
        request.setHoraInicio("09:30");
        request.setHoraFin("09:45");
        request.setColor("#ffffff");
        request.setRecordatorioMinutos(10);
        request.setTableroId(1L);

        when(reunionRepository.findById(10L)).thenReturn(Optional.of(reunion));
        when(reunionRepository.save(any(Reunion.class))).thenAnswer(inv -> inv.getArgument(0));

        ReunionDTO result = reunionService.actualizar(10L, request);

        assertNotNull(result);
        assertEquals("Daily Mod", result.getTitulo());
        assertEquals("New desc", result.getDescripcion());
        assertEquals("09:30", result.getHoraInicio());
        assertTrue(result.getTicketIds().isEmpty());
    }

    @Test
    public void testActualizar_NoEncontrada() {
        ReunionRequest request = new ReunionRequest();
        request.setTitulo("Daily Mod");
        request.setDescripcion("New desc");
        request.setFecha(LocalDate.now());
        request.setHoraInicio("09:30");
        request.setHoraFin("09:45");
        request.setColor("#ffffff");
        request.setRecordatorioMinutos(10);
        request.setTableroId(1L);

        when(reunionRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> reunionService.actualizar(10L, request));
    }

    @Test
    public void testEliminar_Exitoso() {
        when(reunionRepository.existsById(10L)).thenReturn(true);

        reunionService.eliminar(10L);

        verify(reunionRepository, times(1)).deleteById(10L);
    }

    @Test
    public void testEliminar_NoEncontrada() {
        when(reunionRepository.existsById(10L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> reunionService.eliminar(10L));
    }
}
