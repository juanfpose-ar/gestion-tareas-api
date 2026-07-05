package com.gestortareas.api.ticket.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.gestortareas.api.estado.entity.EstadoTablero;
import com.gestortareas.api.estado.repository.EstadoTableroRepository;
import com.gestortareas.api.ticket.dto.TicketDTO;
import com.gestortareas.api.ticket.dto.TicketRequest;
import com.gestortareas.api.ticket.entity.Ticket;
import com.gestortareas.api.ticket.repository.TicketRepository;
import com.gestortareas.api.recordatorio.entity.Recordatorio;
import com.gestortareas.api.recordatorio.repository.RecordatorioRepository;
import com.gestortareas.api.tablero.entity.Tablero;
import com.gestortareas.api.tablero.repository.TableroRepository;
import com.gestortareas.api.etiqueta.repository.EtiquetaRepository;
import com.gestortareas.api.vinculo.service.VinculoService;
import com.gestortareas.api.usuario.repository.UsuarioRepository;
import com.gestortareas.api.checklist.repository.ChecklistItemRepository;
import com.gestortareas.api.nota.repository.NotaTareaRepository;
import com.gestortareas.api.adjunto.repository.AdjuntoRepository;
import com.gestortareas.api.exceptions.BusinessValidationException;
import com.gestortareas.api.version.entity.Version;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class TicketServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private TableroRepository tableroRepository;
    @Mock
    private EstadoTableroRepository estadoRepository;
    @Mock
    private EtiquetaRepository etiquetaRepository;
    @Mock
    private VinculoService vinculoService;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ChecklistItemRepository checklistItemRepository;
    @Mock
    private NotaTareaRepository notaTareaRepository;
    @Mock
    private RecordatorioRepository recordatorioRepository;
    @Mock
    private AdjuntoRepository adjuntoRepository;

    @InjectMocks
    private TicketServiceImpl ticketService;

    @Test
    public void testActualizarTicket_FechaVencimientoSinCambios_NoEliminaRecordatorios() {
        Long ticketId = 1L;
        LocalDateTime fecha = LocalDateTime.of(2026, 7, 31, 0, 0);
        Ticket ticket = Ticket.builder()
                .id(ticketId)
                .titulo("Test Ticket")
                .fechaVencimiento(fecha)
                .tablero(Tablero.builder().id(1L).build())
                .estado(EstadoTablero.builder().id(1L).nombre("Estado 1").build())
                .build();

        TicketRequest request = new TicketRequest();
        request.setTitulo("Test Ticket Modificado");
        request.setEstadoId(1L);
        request.setFechaVencimiento("2026-07-31T00:00:00"); // misma fecha

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(estadoRepository.findById(1L)).thenReturn(Optional.of(ticket.getEstado()));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        TicketDTO result = ticketService.actualizarTicket(ticketId, request, 1L);

        assertNotNull(result);
        verify(recordatorioRepository, never()).findByTicketId(anyLong());
        verify(recordatorioRepository, never()).deleteAll(anyList());
    }

    @Test
    public void testActualizarTicket_FechaVencimientoCambiada_EliminaRecordatorios() {
        Long ticketId = 1L;
        LocalDateTime fechaOld = LocalDateTime.of(2026, 7, 30, 0, 0);
        Ticket ticket = Ticket.builder()
                .id(ticketId)
                .titulo("Test Ticket")
                .fechaVencimiento(fechaOld)
                .tablero(Tablero.builder().id(1L).build())
                .estado(EstadoTablero.builder().id(1L).nombre("Estado 1").build())
                .build();

        TicketRequest request = new TicketRequest();
        request.setTitulo("Test Ticket");
        request.setEstadoId(1L);
        request.setFechaVencimiento("2026-07-31T00:00:00"); // cambia fecha

        Recordatorio r1 = new Recordatorio();
        r1.setId(10L);
        r1.setTicket(ticket);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(estadoRepository.findById(1L)).thenReturn(Optional.of(ticket.getEstado()));
        when(recordatorioRepository.findByTicketId(ticketId)).thenReturn(List.of(r1));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        TicketDTO result = ticketService.actualizarTicket(ticketId, request, 1L);

        assertNotNull(result);
        verify(recordatorioRepository, times(1)).findByTicketId(ticketId);
        verify(recordatorioRepository, times(1)).deleteAll(List.of(r1));
    }

    @Test
    public void testActualizarTicket_FechaVencimientoEliminada_EliminaRecordatorios() {
        Long ticketId = 1L;
        LocalDateTime fechaOld = LocalDateTime.of(2026, 7, 30, 0, 0);
        Ticket ticket = Ticket.builder()
                .id(ticketId)
                .titulo("Test Ticket")
                .fechaVencimiento(fechaOld)
                .tablero(Tablero.builder().id(1L).build())
                .estado(EstadoTablero.builder().id(1L).nombre("Estado 1").build())
                .build();

        TicketRequest request = new TicketRequest();
        request.setTitulo("Test Ticket");
        request.setEstadoId(1L);
        request.setFechaVencimiento(""); // vacio (eliminar fecha)

        Recordatorio r1 = new Recordatorio();
        r1.setId(10L);
        r1.setTicket(ticket);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(estadoRepository.findById(1L)).thenReturn(Optional.of(ticket.getEstado()));
        when(recordatorioRepository.findByTicketId(ticketId)).thenReturn(List.of(r1));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        TicketDTO result = ticketService.actualizarTicket(ticketId, request, 1L);

        assertNotNull(result);
        verify(recordatorioRepository, times(1)).findByTicketId(ticketId);
        verify(recordatorioRepository, times(1)).deleteAll(List.of(r1));
    }

    @Test
    public void testActualizarTicket_CambioEstadoConVencimiento_Exito() {
        Long ticketId = 1L;
        Ticket ticket = Ticket.builder()
                .id(ticketId)
                .titulo("Test Ticket")
                .fechaVencimiento(LocalDateTime.of(2026, 7, 31, 0, 0))
                .tablero(Tablero.builder().id(1L).build())
                .estado(EstadoTablero.builder().id(1L).nombre("Estado 1").build())
                .version(Version.builder().id(1L).titulo("v1").build())
                .build();

        TicketRequest request = new TicketRequest();
        request.setTitulo("Test Ticket");
        request.setEstadoId(2L); // Cambio de estado a 2L
        request.setFechaVencimiento("2026-07-31T00:00:00");

        EstadoTablero nuevoEstado = EstadoTablero.builder().id(2L).nombre("Estado 2").tablero(ticket.getTablero())
                .build();

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(estadoRepository.findById(2L)).thenReturn(Optional.of(nuevoEstado));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        TicketDTO result = ticketService.actualizarTicket(ticketId, request, 1L);
        assertNotNull(result);
    }

    @Test
    public void testActualizarTicket_CambioEstadoSinVencimiento_LanzaExcepcion() {
        Long ticketId = 1L;
        Ticket ticket = Ticket.builder()
                .id(ticketId)
                .titulo("Test Ticket")
                .fechaVencimiento(null) // Sin vencimiento
                .tablero(Tablero.builder().id(1L).build())
                .estado(EstadoTablero.builder().id(1L).nombre("Estado 1").build())
                .build();

        TicketRequest request = new TicketRequest();
        request.setTitulo("Test Ticket");
        request.setEstadoId(2L); // Cambio de estado a 2L
        request.setFechaVencimiento("");

        EstadoTablero nuevoEstado = EstadoTablero.builder().id(2L).nombre("Estado 2").tablero(ticket.getTablero())
                .build();

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(estadoRepository.findById(2L)).thenReturn(Optional.of(nuevoEstado));

        assertThrows(BusinessValidationException.class, () -> {
            ticketService.actualizarTicket(ticketId, request, 1L);
        });
    }

    @Test
    public void testCambiarEstado_ConVencimiento_Exito() {
        Long ticketId = 1L;
        Ticket ticket = Ticket.builder()
                .id(ticketId)
                .titulo("Test Ticket")
                .fechaVencimiento(LocalDateTime.of(2026, 7, 31, 0, 0))
                .tablero(Tablero.builder().id(1L).build())
                .estado(EstadoTablero.builder().id(1L).nombre("Estado 1").build())
                .version(Version.builder().id(1L).titulo("v1").build())
                .build();

        EstadoTablero nuevoEstado = EstadoTablero.builder().id(2L).nombre("Estado 2").tablero(ticket.getTablero())
                .build();

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(estadoRepository.findById(2L)).thenReturn(Optional.of(nuevoEstado));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        TicketDTO result = ticketService.cambiarEstado(ticketId, 2L);
        assertNotNull(result);
    }

    @Test
    public void testCambiarEstado_SinVencimiento_LanzaExcepcion() {
        Long ticketId = 1L;
        Ticket ticket = Ticket.builder()
                .id(ticketId)
                .titulo("Test Ticket")
                .fechaVencimiento(null) // Sin vencimiento
                .tablero(Tablero.builder().id(1L).build())
                .estado(EstadoTablero.builder().id(1L).nombre("Estado 1").build())
                .build();

        EstadoTablero nuevoEstado = EstadoTablero.builder().id(2L).nombre("Estado 2").tablero(ticket.getTablero())
                .build();

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(estadoRepository.findById(2L)).thenReturn(Optional.of(nuevoEstado));

        assertThrows(BusinessValidationException.class, () -> {
            ticketService.cambiarEstado(ticketId, 2L);
        });
    }

    @Test
    public void testCrearTicket_MapeaVersionId() {
        // La versión ya NO viene en TicketRequest; se asigna a través de
        // VersionService.
        // Este test verifica que si el ticket guardado tiene una Version entity
        // asociada,
        // el DTO resultante expone su versionId correctamente.
        TicketRequest request = new TicketRequest();
        request.setTitulo("Test Ticket con Versión");
        request.setTableroId(1L);
        request.setEstadoId(1L);

        Tablero tablero = Tablero.builder().id(1L).build();
        EstadoTablero estado = EstadoTablero.builder().id(1L).nombre("Estado 1").tablero(tablero).build();
        Version version = Version.builder().id(5L).titulo("v1.2.3")
                .fechaVencimiento(LocalDateTime.now().plusMonths(1))
                .tablero(tablero).build();
        Ticket savedTicket = Ticket.builder()
                .id(10L)
                .titulo("Test Ticket con Versión")
                .tablero(tablero)
                .estado(estado)
                .version(version)
                .build();

        when(tableroRepository.findById(1L)).thenReturn(Optional.of(tablero));
        when(estadoRepository.findById(1L)).thenReturn(Optional.of(estado));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);

        TicketDTO result = ticketService.crearTicket(request, 1L);

        assertNotNull(result);
        assertEquals(5L, result.getVersionId());
    }

    @Test
    public void testActualizarTicket_PreservaVersionExistente() {
        // La versión ya NO se actualiza a través de TicketRequest; VersionService la
        // gestiona.
        // Este test verifica que al actualizar título/estado de un ticket con versión
        // asignada,
        // la version entity del ticket no se pierde.
        Long ticketId = 1L;
        Tablero tablero = Tablero.builder().id(1L).build();
        EstadoTablero estado = EstadoTablero.builder().id(1L).nombre("Estado 1").tablero(tablero).build();
        Version version = Version.builder().id(3L).titulo("v1.0.0")
                .fechaVencimiento(LocalDateTime.now().plusMonths(1))
                .tablero(tablero).build();
        Ticket ticket = Ticket.builder()
                .id(ticketId)
                .titulo("Test Ticket")
                .tablero(tablero)
                .estado(estado)
                .version(version)
                .build();

        TicketRequest request = new TicketRequest();
        request.setTitulo("Test Ticket Modificado");
        request.setEstadoId(1L);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(estadoRepository.findById(1L)).thenReturn(Optional.of(estado));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        TicketDTO result = ticketService.actualizarTicket(ticketId, request, 1L);

        assertNotNull(result);
        assertEquals(3L, ticket.getVersion().getId());
    }
}
