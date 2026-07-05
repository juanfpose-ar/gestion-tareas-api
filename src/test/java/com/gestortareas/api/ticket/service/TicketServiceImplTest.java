package com.gestortareas.api.ticket.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.gestortareas.api.enums.Prioridad;
import com.gestortareas.api.enums.Rol;
import com.gestortareas.api.estado.entity.EstadoTablero;
import com.gestortareas.api.estado.repository.EstadoTableroRepository;
import com.gestortareas.api.etiqueta.repository.EtiquetaRepository;
import com.gestortareas.api.ticket.dto.TicketCardDTO;
import com.gestortareas.api.ticket.dto.TicketDTO;
import com.gestortareas.api.ticket.dto.TicketRequest;
import com.gestortareas.api.ticket.entity.Ticket;
import com.gestortareas.api.ticket.repository.TicketRepository;
import com.gestortareas.api.recordatorio.repository.RecordatorioRepository;
import com.gestortareas.api.tablero.entity.Tablero;
import com.gestortareas.api.tablero.repository.TableroRepository;
import com.gestortareas.api.vinculo.service.VinculoService;
import com.gestortareas.api.usuario.entity.Usuario;
import com.gestortareas.api.usuario.repository.UsuarioRepository;
import com.gestortareas.api.checklist.repository.ChecklistItemRepository;
import com.gestortareas.api.nota.repository.NotaTareaRepository;
import com.gestortareas.api.adjunto.repository.AdjuntoRepository;
import com.gestortareas.api.exceptions.BusinessValidationException;
import com.gestortareas.api.exceptions.ConflictException;
import com.gestortareas.api.exceptions.EntityNotFoundException;
import com.gestortareas.api.version.entity.Version;
import com.gestortareas.api.version.entity.VersionEstado;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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

    private Tablero tablero;
    private EstadoTablero estado;
    private Ticket ticket;
    private Usuario usuario;

    @BeforeEach
    public void setup() {
        tablero = Tablero.builder().id(1L).titulo("Work Board").build();
        estado = EstadoTablero.builder().id(1L).nombre("Por Hacer").tablero(tablero).build();
        usuario = Usuario.builder().id(10L).username("john.doe").nombreCompleto("John Doe").rol(Rol.USER).build();
        ticket = Ticket.builder()
                .id(100L)
                .titulo("Original Ticket")
                .descripcion("Desc")
                .prioridad(Prioridad.MEDIA)
                .tablero(tablero)
                .estado(estado)
                .archivado(false)
                .completado(false)
                .orden(1)
                .creador(usuario)
                .fechaCreacion(LocalDateTime.now())
                .fechaModificacion(LocalDateTime.now().minusDays(1))
                .etiquetas(new HashSet<>())
                .asignados(new HashSet<>())
                .informados(new HashSet<>())
                .build();
    }

    @Test
    public void testListarActivosPorTablero() {
        when(ticketRepository.findByTableroIdAndArchivadoFalseOrderByOrdenAscIdAsc(1L)).thenReturn(List.of(ticket));
        List<TicketCardDTO> result = ticketService.listarActivosPorTablero(1L);
        assertEquals(1, result.size());
        assertEquals("Original Ticket", result.get(0).getTitulo());
    }

    @Test
    public void testListarArchivadosPorTablero() {
        when(ticketRepository.findByTableroIdAndArchivadoTrueOrderByFechaArchivadoDesc(1L)).thenReturn(List.of(ticket));
        List<TicketCardDTO> result = ticketService.listarArchivadosPorTablero(1L);
        assertEquals(1, result.size());
    }

    @Test
    public void testListarPorVersion() {
        when(ticketRepository.findByVersionId(3L)).thenReturn(List.of(ticket));
        List<TicketCardDTO> result = ticketService.listarPorVersion(3L);
        assertEquals(1, result.size());
    }

    @Test
    public void testObtenerPorId_Exitoso() {
        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        TicketDTO result = ticketService.obtenerPorId(100L);
        assertNotNull(result);
        assertEquals("Original Ticket", result.getTitulo());
    }

    @Test
    public void testObtenerPorId_NoEncontrado() {
        when(ticketRepository.findById(100L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> ticketService.obtenerPorId(100L));
    }

    @Test
    public void testCrearTicket_Exitoso() {
        TicketRequest request = new TicketRequest();
        request.setTableroId(1L);
        request.setEstadoId(1L);
        request.setTitulo("New Ticket");
        request.setPrioridad(Prioridad.MEDIA);
        request.setFechaVencimiento(LocalDate.now().plusDays(5).toString());

        when(tableroRepository.findById(1L)).thenReturn(Optional.of(tablero));
        when(estadoRepository.findById(1L)).thenReturn(Optional.of(estado));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        TicketDTO result = ticketService.crearTicket(request, 10L);
        assertNotNull(result);
        assertEquals("New Ticket", result.getTitulo());
    }

    @Test
    public void testCrearTicket_EstadoDiferenteTablero() {
        TicketRequest request = new TicketRequest();
        request.setTableroId(1L);
        request.setEstadoId(2L);
        request.setTitulo("New Ticket");

        Tablero tablero2 = Tablero.builder().id(2L).build();
        EstadoTablero estado2 = EstadoTablero.builder().id(2L).tablero(tablero2).build();

        when(tableroRepository.findById(1L)).thenReturn(Optional.of(tablero));
        when(estadoRepository.findById(2L)).thenReturn(Optional.of(estado2));

        assertThrows(BusinessValidationException.class, () -> ticketService.crearTicket(request, 10L));
    }

    @Test
    public void testCrearTicket_VencimientoHoyOAntes() {
        TicketRequest request = new TicketRequest();
        request.setTableroId(1L);
        request.setEstadoId(1L);
        request.setTitulo("New Ticket");
        request.setFechaVencimiento(LocalDate.now().toString());

        when(tableroRepository.findById(1L)).thenReturn(Optional.of(tablero));
        when(estadoRepository.findById(1L)).thenReturn(Optional.of(estado));

        assertThrows(BusinessValidationException.class, () -> ticketService.crearTicket(request, 10L));
    }

    @Test
    public void testActualizarTicket_ConflictException() {
        TicketRequest request = new TicketRequest();
        request.setEstadoId(1L);
        request.setFechaModificacion(LocalDateTime.now().toString());

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));

        assertThrows(ConflictException.class, () -> ticketService.actualizarTicket(100L, request, 10L));
    }

    @Test
    public void testActualizarTicket_ChangeEstadoSinFechaVencimiento() {
        TicketRequest request = new TicketRequest();
        request.setEstadoId(2L); // original is 1L
        request.setFechaVencimiento("");

        EstadoTablero estado2 = EstadoTablero.builder().id(2L).tablero(tablero).build();

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(estadoRepository.findById(2L)).thenReturn(Optional.of(estado2));

        assertThrows(BusinessValidationException.class, () -> ticketService.actualizarTicket(100L, request, 10L));
    }

    @Test
    public void testActualizarTicket_ChangeEstadoSinVersion() {
        TicketRequest request = new TicketRequest();
        request.setEstadoId(2L);
        request.setFechaVencimiento(LocalDate.now().plusDays(5).toString());

        EstadoTablero estado2 = EstadoTablero.builder().id(2L).tablero(tablero).build();

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(estadoRepository.findById(2L)).thenReturn(Optional.of(estado2));

        assertThrows(BusinessValidationException.class, () -> ticketService.actualizarTicket(100L, request, 10L));
    }

    @Test
    public void testActualizarTicket_CompletadoVersionCerrada() {
        Version version = Version.builder().id(3L).estado(VersionEstado.CERRADO).build();
        ticket.setVersion(version);
        ticket.setCompletado(true);

        TicketRequest request = new TicketRequest();
        request.setEstadoId(1L);
        request.setCompletado(false);

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(estadoRepository.findById(1L)).thenReturn(Optional.of(estado));

        assertThrows(BusinessValidationException.class, () -> ticketService.actualizarTicket(100L, request, 10L));
    }

    @Test
    public void testActualizarTicket_VencimientoAnteriorAHoy() {
        TicketRequest request = new TicketRequest();
        request.setEstadoId(1L);
        request.setFechaVencimiento(LocalDate.now().minusDays(1).toString());

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(estadoRepository.findById(1L)).thenReturn(Optional.of(estado));

        assertThrows(BusinessValidationException.class, () -> ticketService.actualizarTicket(100L, request, 10L));
    }

    @Test
    public void testActualizarTicket_Exitoso() {
        TicketRequest request = new TicketRequest();
        request.setEstadoId(1L);
        request.setTitulo("Updated Title");
        request.setFechaVencimiento(LocalDate.now().plusDays(10).toString());

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(estadoRepository.findById(1L)).thenReturn(Optional.of(estado));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        TicketDTO result = ticketService.actualizarTicket(100L, request, 10L);
        assertNotNull(result);
        assertEquals("Updated Title", result.getTitulo());
    }

    @Test
    public void testCambiarEstado_Exitoso() {
        Version version = Version.builder().id(3L).estado(VersionEstado.POR_HACER).build();
        ticket.setVersion(version);
        ticket.setFechaVencimiento(LocalDateTime.now().plusDays(5));

        EstadoTablero estado2 = EstadoTablero.builder().id(2L).nombre("Hecho").tablero(tablero).build();

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(estadoRepository.findById(2L)).thenReturn(Optional.of(estado2));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        TicketDTO result = ticketService.cambiarEstado(100L, 2L);
        assertNotNull(result);
        assertEquals(2L, result.getEstadoId());
        assertTrue(result.isCompletado()); // state "Hecho" sets completado = true
    }

    @Test
    public void testCambiarEstado_DiferenteTablero() {
        Tablero tablero2 = Tablero.builder().id(2L).build();
        EstadoTablero estado2 = EstadoTablero.builder().id(2L).tablero(tablero2).build();

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(estadoRepository.findById(2L)).thenReturn(Optional.of(estado2));

        assertThrows(BusinessValidationException.class, () -> ticketService.cambiarEstado(100L, 2L));
    }

    @Test
    public void testCambiarEstado_SinVencimiento() {
        EstadoTablero estado2 = EstadoTablero.builder().id(2L).tablero(tablero).build();

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(estadoRepository.findById(2L)).thenReturn(Optional.of(estado2));

        assertThrows(BusinessValidationException.class, () -> ticketService.cambiarEstado(100L, 2L));
    }

    @Test
    public void testCambiarEstado_SinVersion() {
        ticket.setFechaVencimiento(LocalDateTime.now().plusDays(5));
        EstadoTablero estado2 = EstadoTablero.builder().id(2L).tablero(tablero).build();

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(estadoRepository.findById(2L)).thenReturn(Optional.of(estado2));

        assertThrows(BusinessValidationException.class, () -> ticketService.cambiarEstado(100L, 2L));
    }

    @Test
    public void testReordenarEnEstado() {
        Ticket t1 = Ticket.builder().id(101L).build();
        Ticket t2 = Ticket.builder().id(102L).build();
        when(ticketRepository.findByEstadoIdAndArchivadoFalse(1L)).thenReturn(List.of(t1, t2));

        ticketService.reordenarEnEstado(1L, List.of(102L, 101L));

        assertEquals(2, t1.getOrden());
        assertEquals(1, t2.getOrden());
        verify(ticketRepository, times(2)).save(any(Ticket.class));
    }

    @Test
    public void testCambiarArchivado() {
        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        TicketDTO result = ticketService.cambiarArchivado(100L, true);

        assertTrue(result.isArchivado());
        assertNotNull(result.getFechaArchivado());
    }

    @Test
    public void testCambiarCompletado_ClosedVersion() {
        Version version = Version.builder().id(3L).estado(VersionEstado.CERRADO).build();
        ticket.setVersion(version);

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));

        assertThrows(BusinessValidationException.class, () -> ticketService.cambiarCompletado(100L, false));
    }

    @Test
    public void testEliminarTicket_Exitoso() {
        when(ticketRepository.existsById(100L)).thenReturn(true);

        ticketService.eliminarTicket(100L);

        verify(ticketRepository, times(1)).deleteById(100L);
    }

    @Test
    public void testEliminarTicket_NoEncontrado() {
        when(ticketRepository.existsById(100L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> ticketService.eliminarTicket(100L));
    }

    @Test
    public void testAsignarUsuario() {
        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        TicketDTO result = ticketService.asignarUsuario(100L, 10L);

        assertNotNull(result);
        assertEquals(1, ticket.getAsignados().size());
    }

    @Test
    public void testDesasignarUsuario() {
        ticket.getAsignados().add(usuario);
        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        TicketDTO result = ticketService.desasignarUsuario(100L, 10L);

        assertNotNull(result);
        assertTrue(ticket.getAsignados().isEmpty());
    }

    @Test
    public void testAsignarInformado() {
        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        TicketDTO result = ticketService.asignarInformado(100L, 10L);

        assertNotNull(result);
        assertEquals(1, ticket.getInformados().size());
    }

    @Test
    public void testDesasignarInformado() {
        ticket.getInformados().add(usuario);
        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        TicketDTO result = ticketService.desasignarInformado(100L, 10L);

        assertNotNull(result);
        assertTrue(ticket.getInformados().isEmpty());
    }
}
