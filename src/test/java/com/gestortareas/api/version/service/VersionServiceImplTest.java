package com.gestortareas.api.version.service;

import com.gestortareas.api.exceptions.BusinessValidationException;
import com.gestortareas.api.tablero.entity.Tablero;
import com.gestortareas.api.tablero.repository.TableroRepository;
import com.gestortareas.api.ticket.entity.Ticket;
import com.gestortareas.api.ticket.repository.TicketRepository;
import com.gestortareas.api.version.dto.VersionDTO;
import com.gestortareas.api.version.dto.VersionRequest;
import com.gestortareas.api.version.entity.Version;
import com.gestortareas.api.version.entity.VersionEstado;
import com.gestortareas.api.version.repository.VersionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VersionServiceImplTest {

    @Mock
    private VersionRepository versionRepository;

    @Mock
    private TableroRepository tableroRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private VersionServiceImpl versionService;

    private Tablero tablero;
    private Version version;
    private Ticket ticket;

    @BeforeEach
    public void setup() {
        tablero = Tablero.builder().id(1L).titulo("Tablero Test").build();
        version = Version.builder()
                .id(10L)
                .titulo("v1.0")
                .fechaVencimiento(LocalDateTime.now().plusDays(10))
                .tablero(tablero)
                .estado(VersionEstado.POR_HACER)
                .build();
        ticket = Ticket.builder().id(100L).titulo("Ticket 1").completado(false).build();
    }

    @Test
    public void testListarPorTablero() {
        when(versionRepository.findByTableroIdOrderByFechaVencimientoAsc(1L)).thenReturn(List.of(version));
        when(ticketRepository.findByVersionId(10L)).thenReturn(List.of(ticket));

        List<VersionDTO> result = versionService.listarPorTablero(1L);

        assertEquals(1, result.size());
        assertEquals("v1.0", result.get(0).getTitulo());
        assertEquals(10L, result.get(0).getId());
    }

    @Test
    public void testCrearVersion_Exitoso() {
        VersionRequest request = new VersionRequest();
        request.setTitulo("v2.0");
        request.setFechaVencimiento(LocalDate.now().plusDays(5).toString());
        request.setTableroId(1L);
        request.setTicketIds(List.of(100L));
        request.setEstado(VersionEstado.POR_HACER);

        when(tableroRepository.findById(1L)).thenReturn(Optional.of(tablero));
        when(versionRepository.save(any(Version.class))).thenAnswer(inv -> {
            Version v = inv.getArgument(0);
            v.setId(11L);
            return v;
        });
        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.findByVersionId(11L)).thenReturn(List.of(ticket));

        VersionDTO result = versionService.crearVersion(request);

        assertNotNull(result);
        assertEquals(11L, result.getId());
        assertEquals("v2.0", result.getTitulo());
        verify(ticketRepository, times(1)).save(ticket);
    }

    @Test
    public void testCrearVersion_TableroNoEncontrado() {
        VersionRequest request = new VersionRequest();
        request.setTitulo("v2.0");
        request.setFechaVencimiento(LocalDate.now().plusDays(5).toString());
        request.setTableroId(1L);

        when(tableroRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> versionService.crearVersion(request));
    }

    @Test
    public void testCrearVersion_FechaVencimientoInvalida() {
        VersionRequest request = new VersionRequest();
        request.setTitulo("v2.0");
        request.setFechaVencimiento(LocalDate.now().minusDays(5).toString());
        request.setTableroId(1L);

        when(tableroRepository.findById(1L)).thenReturn(Optional.of(tablero));

        assertThrows(BusinessValidationException.class, () -> versionService.crearVersion(request));
    }

    @Test
    public void testCrearVersion_FechaVencimientoParseError() {
        VersionRequest request = new VersionRequest();
        request.setTitulo("v2.0");
        request.setFechaVencimiento("invalid-date");
        request.setTableroId(1L);

        when(tableroRepository.findById(1L)).thenReturn(Optional.of(tablero));

        assertThrows(BusinessValidationException.class, () -> versionService.crearVersion(request));
    }

    @Test
    public void testCrearVersion_DuplicadoEnCurso() {
        VersionRequest request = new VersionRequest();
        request.setTitulo("v2.0");
        request.setFechaVencimiento(LocalDate.now().plusDays(5).toString());
        request.setTableroId(1L);
        request.setEstado(VersionEstado.EN_CURSO);

        when(tableroRepository.findById(1L)).thenReturn(Optional.of(tablero));
        when(versionRepository.existsByTableroIdAndEstado(1L, VersionEstado.EN_CURSO)).thenReturn(true);

        assertThrows(BusinessValidationException.class, () -> versionService.crearVersion(request));
    }

    @Test
    public void testActualizarVersion_Exitoso() {
        VersionRequest request = new VersionRequest();
        request.setTitulo("v1.0 Mod");
        request.setFechaVencimiento(LocalDate.now().plusDays(10).toString());
        request.setTableroId(1L);
        request.setTicketIds(List.of(100L));
        request.setEstado(VersionEstado.EN_CURSO);

        when(versionRepository.findById(10L)).thenReturn(Optional.of(version));
        when(versionRepository.save(any(Version.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ticketRepository.findByVersionId(10L)).thenReturn(new ArrayList<>());
        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));

        VersionDTO result = versionService.actualizarVersion(10L, request);

        assertNotNull(result);
        assertEquals("v1.0 Mod", result.getTitulo());
        assertEquals(VersionEstado.EN_CURSO, result.getEstado());
    }

    @Test
    public void testActualizarVersion_RevertirCerrado() {
        version.setEstado(VersionEstado.CERRADO);
        VersionRequest request = new VersionRequest();
        request.setTitulo("v1.0 Mod");
        request.setFechaVencimiento(LocalDate.now().plusDays(10).toString());
        request.setTableroId(1L);
        request.setEstado(VersionEstado.POR_HACER);

        when(versionRepository.findById(10L)).thenReturn(Optional.of(version));

        assertThrows(BusinessValidationException.class, () -> versionService.actualizarVersion(10L, request));
    }

    @Test
    public void testActualizarVersion_EnCursoACerradoDirecto() {
        version.setEstado(VersionEstado.EN_CURSO);
        VersionRequest request = new VersionRequest();
        request.setTitulo("v1.0 Mod");
        request.setFechaVencimiento(LocalDate.now().plusDays(10).toString());
        request.setTableroId(1L);
        request.setEstado(VersionEstado.CERRADO);

        when(versionRepository.findById(10L)).thenReturn(Optional.of(version));

        assertThrows(BusinessValidationException.class, () -> versionService.actualizarVersion(10L, request));
    }

    @Test
    public void testActualizarVersion_CompletarConTicketsPendientes() {
        version.setEstado(VersionEstado.EN_CURSO);
        VersionRequest request = new VersionRequest();
        request.setTitulo("v1.0 Mod");
        request.setFechaVencimiento(LocalDate.now().plusDays(10).toString());
        request.setTableroId(1L);
        request.setEstado(VersionEstado.FINALIZADO);

        when(versionRepository.findById(10L)).thenReturn(Optional.of(version));
        when(ticketRepository.findByVersionId(10L)).thenReturn(List.of(ticket)); // ticket is not completado

        assertThrows(BusinessValidationException.class, () -> versionService.actualizarVersion(10L, request));
    }

    @Test
    public void testActualizarVersion_DuplicadoEnCurso() {
        VersionRequest request = new VersionRequest();
        request.setTitulo("v1.0 Mod");
        request.setTableroId(1L);
        request.setFechaVencimiento(LocalDate.now().plusDays(10).toString());
        request.setEstado(VersionEstado.EN_CURSO);

        when(versionRepository.findById(10L)).thenReturn(Optional.of(version));
        when(versionRepository.existsByTableroIdAndEstadoAndIdNot(1L, VersionEstado.EN_CURSO, 10L)).thenReturn(true);

        assertThrows(BusinessValidationException.class, () -> versionService.actualizarVersion(10L, request));
    }

    @Test
    public void testEliminarVersion_Exitoso() {
        when(versionRepository.findById(10L)).thenReturn(Optional.of(version));
        when(ticketRepository.findByVersionId(10L)).thenReturn(List.of(ticket));

        versionService.eliminarVersion(10L);

        assertNull(ticket.getVersion());
        verify(ticketRepository, times(1)).save(ticket);
        verify(versionRepository, times(1)).delete(version);
    }

    @Test
    public void testEliminarVersion_NoEncontrada() {
        when(versionRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> versionService.eliminarVersion(10L));
    }

    @Test
    public void testActualizarVersion_CerradaModificarTickets() {
        version.setEstado(VersionEstado.CERRADO);
        VersionRequest request = new VersionRequest();
        request.setTitulo("v1.0 Mod");
        request.setFechaVencimiento(LocalDate.now().plusDays(10).toString());
        request.setTableroId(1L);
        request.setEstado(VersionEstado.CERRADO);
        request.setTicketIds(List.of(200L)); // different from linked ticket 100L

        when(versionRepository.findById(10L)).thenReturn(Optional.of(version));
        when(ticketRepository.findByVersionId(10L)).thenReturn(List.of(ticket));

        assertThrows(BusinessValidationException.class, () -> versionService.actualizarVersion(10L, request));
    }

    @Test
    public void testActualizarVersion_VincularTicketNoCompletadoAVersionCerrada() {
        VersionRequest request = new VersionRequest();
        request.setTitulo("v1.0 Mod");
        request.setFechaVencimiento(LocalDate.now().plusDays(10).toString());
        request.setTableroId(1L);
        request.setEstado(VersionEstado.CERRADO);
        request.setTicketIds(List.of(100L));

        when(versionRepository.findById(10L)).thenReturn(Optional.of(version));
        when(versionRepository.save(any(Version.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ticketRepository.findByVersionId(10L)).thenReturn(new ArrayList<>());
        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket)); // ticket is not completado

        assertThrows(BusinessValidationException.class, () -> versionService.actualizarVersion(10L, request));
    }

    @Test
    public void testActualizarVersion_VincularTicketDeOtraVersionAEnCurso() {
        VersionRequest request = new VersionRequest();
        request.setTitulo("v1.0 Mod");
        request.setFechaVencimiento(LocalDate.now().plusDays(10).toString());
        request.setTableroId(1L);
        request.setEstado(VersionEstado.EN_CURSO);
        request.setTicketIds(List.of(100L));

        Version otherVer = Version.builder().id(20L).estado(VersionEstado.FINALIZADO).build();
        ticket.setVersion(otherVer);

        when(versionRepository.findById(10L)).thenReturn(Optional.of(version));
        when(versionRepository.save(any(Version.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ticketRepository.findByVersionId(10L)).thenReturn(new ArrayList<>());
        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));

        assertThrows(BusinessValidationException.class, () -> versionService.actualizarVersion(10L, request));
    }

    @Test
    public void testActualizarVersion_VincularTicketDeOtraVersionAPorHacer() {
        VersionRequest request = new VersionRequest();
        request.setTitulo("v1.0 Mod");
        request.setFechaVencimiento(LocalDate.now().plusDays(10).toString());
        request.setTableroId(1L);
        request.setEstado(VersionEstado.POR_HACER);
        request.setTicketIds(List.of(100L));

        Version otherVer = Version.builder().id(20L).estado(VersionEstado.FINALIZADO).build();
        ticket.setVersion(otherVer);

        when(versionRepository.findById(10L)).thenReturn(Optional.of(version));
        when(versionRepository.save(any(Version.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ticketRepository.findByVersionId(10L)).thenReturn(new ArrayList<>());
        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));

        assertThrows(BusinessValidationException.class, () -> versionService.actualizarVersion(10L, request));
    }
}
