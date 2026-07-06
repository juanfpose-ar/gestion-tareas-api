package com.gestortareas.api.notificaciones.service;

import com.gestortareas.api.enums.Rol;
import com.gestortareas.api.enums.TipoRecordatorio;
import com.gestortareas.api.mensajeria.repository.UsuarioConversacionRepository;
import com.gestortareas.api.recordatorio.entity.Recordatorio;
import com.gestortareas.api.recordatorio.repository.RecordatorioRepository;
import com.gestortareas.api.reunion.entity.Reunion;
import com.gestortareas.api.reunion.repository.ReunionRepository;
import com.gestortareas.api.tablero.entity.Tablero;
import com.gestortareas.api.ticket.entity.Ticket;
import com.gestortareas.api.ticket.repository.TicketRepository;
import com.gestortareas.api.usuario.entity.Usuario;
import com.gestortareas.api.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DigestServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private RecordatorioRepository recordatorioRepository;

    @Mock
    private ReunionRepository reunionRepository;

    @Mock
    private UsuarioConversacionRepository usuarioConversacionRepository;

    @Mock
    private MailService mailService;

    @InjectMocks
    private DigestService digestService;

    private Tablero tablero;
    private Usuario usuario;

    @BeforeEach
    public void setup() {
        tablero = Tablero.builder().id(1L).titulo("Tablero Test").build();
        usuario = Usuario.builder()
                .id(10L)
                .username("user.test")
                .nombreCompleto("User Test")
                .email("user@test.com")
                .rol(Rol.USER)
                .activo(true)
                .tablerosAsignados(new HashSet<>(Set.of(tablero)))
                .build();
    }

    private void sinNovedadesBase() {
        when(ticketRepository.findRelevantesParaUsuario(10L)).thenReturn(List.of());
        when(recordatorioRepository.findVencidosParaUsuario(eq(10L), any(), any())).thenReturn(List.of());
        when(usuarioConversacionRepository.countByUsuarioIdAndLeidaFalseAndArchivadaFalseAndEliminadaFalse(10L))
                .thenReturn(0);
    }

    @Test
    public void testUsuarioSinEmail_SeSaltea() {
        usuario.setEmail(null);
        when(usuarioRepository.findByActivoTrue()).thenReturn(List.of(usuario));
        when(reunionRepository.findByFechaBetween(any(), any())).thenReturn(List.of());

        digestService.enviarDigestATodos();

        verifyNoInteractions(mailService);
        verify(ticketRepository, never()).findRelevantesParaUsuario(anyLong());
    }

    @Test
    public void testSinNovedades_NoEnviaNiActualizaMarca() {
        when(usuarioRepository.findByActivoTrue()).thenReturn(List.of(usuario));
        when(reunionRepository.findByFechaBetween(any(), any())).thenReturn(List.of());
        sinNovedadesBase();

        digestService.enviarDigestATodos();

        verifyNoInteractions(mailService);
        verify(usuarioRepository, never()).save(any());
        assertNull(usuario.getUltimoDigestEnviado());
    }

    @Test
    public void testTicketActualizado_EnviaYMarcaUltimoDigest() {
        Ticket ticket = Ticket.builder()
                .id(5L).titulo("Arreglar login").tablero(tablero)
                .fechaModificacion(LocalDateTime.now().minusHours(1))
                .build();
        when(usuarioRepository.findByActivoTrue()).thenReturn(List.of(usuario));
        when(reunionRepository.findByFechaBetween(any(), any())).thenReturn(List.of());
        when(ticketRepository.findRelevantesParaUsuario(10L)).thenReturn(List.of(ticket));
        when(recordatorioRepository.findVencidosParaUsuario(eq(10L), any(), any())).thenReturn(List.of());
        when(usuarioConversacionRepository.countByUsuarioIdAndLeidaFalseAndArchivadaFalseAndEliminadaFalse(10L))
                .thenReturn(0);

        digestService.enviarDigestATodos();

        ArgumentCaptor<String> cuerpo = ArgumentCaptor.forClass(String.class);
        verify(mailService).enviar(eq("user@test.com"), anyString(), cuerpo.capture());
        assertTrue(cuerpo.getValue().contains("TICKETS ACTUALIZADOS"));
        assertTrue(cuerpo.getValue().contains("Arreglar login"));
        assertNotNull(usuario.getUltimoDigestEnviado());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    public void testTicketVencido_ApareceEnElCuerpo() {
        Ticket vencido = Ticket.builder()
                .id(6L).titulo("Entregar informe").tablero(tablero)
                .fechaVencimiento(LocalDateTime.now().minusHours(2))
                .build();
        when(usuarioRepository.findByActivoTrue()).thenReturn(List.of(usuario));
        when(reunionRepository.findByFechaBetween(any(), any())).thenReturn(List.of());
        when(ticketRepository.findRelevantesParaUsuario(10L)).thenReturn(List.of(vencido));
        when(recordatorioRepository.findVencidosParaUsuario(eq(10L), any(), any())).thenReturn(List.of());
        when(usuarioConversacionRepository.countByUsuarioIdAndLeidaFalseAndArchivadaFalseAndEliminadaFalse(10L))
                .thenReturn(0);

        digestService.enviarDigestATodos();

        ArgumentCaptor<String> cuerpo = ArgumentCaptor.forClass(String.class);
        verify(mailService).enviar(anyString(), anyString(), cuerpo.capture());
        assertTrue(cuerpo.getValue().contains("TICKETS VENCIDOS"));
        assertTrue(cuerpo.getValue().contains("Entregar informe"));
    }

    @Test
    public void testRecordatorioVencido_ApareceEnElCuerpo() {
        Ticket ticket = Ticket.builder().id(7L).titulo("Ticket con recordatorio").tablero(tablero).build();
        Recordatorio recordatorio = new Recordatorio(
                20L, ticket, TipoRecordatorio.PERSONALIZADO, LocalDateTime.now().minusMinutes(30), false);
        when(usuarioRepository.findByActivoTrue()).thenReturn(List.of(usuario));
        when(reunionRepository.findByFechaBetween(any(), any())).thenReturn(List.of());
        when(ticketRepository.findRelevantesParaUsuario(10L)).thenReturn(List.of());
        when(recordatorioRepository.findVencidosParaUsuario(eq(10L), any(), any()))
                .thenReturn(List.of(recordatorio));
        when(usuarioConversacionRepository.countByUsuarioIdAndLeidaFalseAndArchivadaFalseAndEliminadaFalse(10L))
                .thenReturn(0);

        digestService.enviarDigestATodos();

        ArgumentCaptor<String> cuerpo = ArgumentCaptor.forClass(String.class);
        verify(mailService).enviar(anyString(), anyString(), cuerpo.capture());
        assertTrue(cuerpo.getValue().contains("RECORDATORIOS DE TICKETS"));
        assertTrue(cuerpo.getValue().contains("Ticket con recordatorio"));
    }

    @Test
    public void testReunionConRecordatorioEnVentana_ApareceEnElCuerpo() {
        LocalDateTime hace30Min = LocalDateTime.now().minusMinutes(30);
        Reunion reunion = Reunion.builder()
                .id(30L).titulo("Daily del equipo")
                .fecha(hace30Min.toLocalDate())
                .horaInicio(String.format("%02d:%02d", hace30Min.getHour(), hace30Min.getMinute()))
                .recordatorioMinutos(0)
                .tablero(tablero)
                .build();
        when(usuarioRepository.findByActivoTrue()).thenReturn(List.of(usuario));
        when(reunionRepository.findByFechaBetween(any(), any())).thenReturn(List.of(reunion));
        sinNovedadesBase();

        digestService.enviarDigestATodos();

        ArgumentCaptor<String> cuerpo = ArgumentCaptor.forClass(String.class);
        verify(mailService).enviar(anyString(), anyString(), cuerpo.capture());
        assertTrue(cuerpo.getValue().contains("RECORDATORIOS DE REUNIONES"));
        assertTrue(cuerpo.getValue().contains("Daily del equipo"));
    }

    @Test
    public void testReunionDeOtroTablero_NoSeIncluye() {
        Tablero otroTablero = Tablero.builder().id(99L).titulo("Ajeno").build();
        LocalDateTime hace30Min = LocalDateTime.now().minusMinutes(30);
        Reunion reunion = Reunion.builder()
                .id(31L).titulo("Reunión ajena")
                .fecha(hace30Min.toLocalDate())
                .horaInicio(String.format("%02d:%02d", hace30Min.getHour(), hace30Min.getMinute()))
                .recordatorioMinutos(0)
                .tablero(otroTablero)
                .build();
        when(usuarioRepository.findByActivoTrue()).thenReturn(List.of(usuario));
        when(reunionRepository.findByFechaBetween(any(), any())).thenReturn(List.of(reunion));
        sinNovedadesBase();

        digestService.enviarDigestATodos();

        verifyNoInteractions(mailService);
    }

    @Test
    public void testMensajesSinLeer_EnviaConElConteo() {
        when(usuarioRepository.findByActivoTrue()).thenReturn(List.of(usuario));
        when(reunionRepository.findByFechaBetween(any(), any())).thenReturn(List.of());
        when(ticketRepository.findRelevantesParaUsuario(10L)).thenReturn(List.of());
        when(recordatorioRepository.findVencidosParaUsuario(eq(10L), any(), any())).thenReturn(List.of());
        when(usuarioConversacionRepository.countByUsuarioIdAndLeidaFalseAndArchivadaFalseAndEliminadaFalse(10L))
                .thenReturn(3);

        digestService.enviarDigestATodos();

        ArgumentCaptor<String> cuerpo = ArgumentCaptor.forClass(String.class);
        verify(mailService).enviar(anyString(), anyString(), cuerpo.capture());
        assertTrue(cuerpo.getValue().contains("3 conversación(es) sin leer"));
    }

    @Test
    public void testErrorConUnUsuario_NoCortaALosDemas() {
        Usuario roto = Usuario.builder()
                .id(11L).username("roto").email("roto@test.com").rol(Rol.USER).activo(true)
                .tablerosAsignados(new HashSet<>())
                .build();
        when(usuarioRepository.findByActivoTrue()).thenReturn(List.of(roto, usuario));
        when(reunionRepository.findByFechaBetween(any(), any())).thenReturn(List.of());
        when(ticketRepository.findRelevantesParaUsuario(11L)).thenThrow(new RuntimeException("boom"));
        when(ticketRepository.findRelevantesParaUsuario(10L)).thenReturn(List.of());
        when(recordatorioRepository.findVencidosParaUsuario(eq(10L), any(), any())).thenReturn(List.of());
        when(usuarioConversacionRepository.countByUsuarioIdAndLeidaFalseAndArchivadaFalseAndEliminadaFalse(10L))
                .thenReturn(1);

        assertDoesNotThrow(() -> digestService.enviarDigestATodos());

        // El usuario sano recibió su digest a pesar del error del primero.
        verify(mailService).enviar(eq("user@test.com"), anyString(), anyString());
    }
}
