package com.gestortareas.api.security;

import com.gestortareas.api.estado.repository.EstadoTableroRepository;
import com.gestortareas.api.etiqueta.entity.Etiqueta;
import com.gestortareas.api.etiqueta.repository.EtiquetaRepository;
import com.gestortareas.api.exceptions.EntityNotFoundException;
import com.gestortareas.api.reunion.repository.ReunionRepository;
import com.gestortareas.api.tablero.entity.Tablero;
import com.gestortareas.api.ticket.repository.TicketRepository;
import com.gestortareas.api.version.repository.VersionRepository;
import com.gestortareas.api.vinculo.repository.TicketVinculoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccesoTableroGuardTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private VersionRepository versionRepository;

    @Mock
    private EstadoTableroRepository estadoTableroRepository;

    @Mock
    private TicketVinculoRepository ticketVinculoRepository;

    @Mock
    private ReunionRepository reunionRepository;

    @Mock
    private EtiquetaRepository etiquetaRepository;

    @Mock
    private MembresiaTableroCacheService membresiaTableroCacheService;

    @InjectMocks
    private AccesoTableroGuard guard;

    @Mock
    private Authentication authentication;

    private void comoAdmin() {
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(authentication).getAuthorities();
    }

    private void comoUsuario(String username) {
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER"))).when(authentication).getAuthorities();
        // lenient: en los caminos que cortan antes (recurso inexistente, etiqueta global)
        // getName() nunca llega a usarse y el strict-stubbing de Mockito lo marcaría como error.
        lenient().when(authentication.getName()).thenReturn(username);
    }

    // ---- Tablero ----

    @Test
    public void testTablero_AdminBypassSinTocarLaBase() {
        comoAdmin();

        assertTrue(guard.puedeAccederATablero(authentication, 1L));

        verifyNoInteractions(membresiaTableroCacheService);
    }

    @Test
    public void testTablero_MiembroPuedeAcceder() {
        comoUsuario("user");
        when(membresiaTableroCacheService.esMiembro("user", 1L)).thenReturn(true);

        assertTrue(guard.puedeAccederATablero(authentication, 1L));
    }

    @Test
    public void testTablero_NoMiembroNoPuedeAcceder() {
        comoUsuario("user");
        when(membresiaTableroCacheService.esMiembro("user", 1L)).thenReturn(false);

        assertFalse(guard.puedeAccederATablero(authentication, 1L));
    }

    // ---- Ticket ----

    @Test
    public void testTicket_ResuelveElTableroYDelegaEnMembresia() {
        comoUsuario("user");
        when(ticketRepository.findTableroIdByTicketId(5L)).thenReturn(Optional.of(1L));
        when(membresiaTableroCacheService.esMiembro("user", 1L)).thenReturn(true);

        assertTrue(guard.puedeAccederATicket(authentication, 5L));
    }

    @Test
    public void testTicket_NoEncontrado() {
        comoUsuario("user");
        when(ticketRepository.findTableroIdByTicketId(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> guard.puedeAccederATicket(authentication, 99L));
    }

    @Test
    public void testTicket_AdminNoConsultaElTicket() {
        comoAdmin();

        assertTrue(guard.puedeAccederATicket(authentication, 99L));

        verifyNoInteractions(ticketRepository);
    }

    // ---- Version / Estado / Vinculo / Reunion ----

    @Test
    public void testVersion_MiembroDelTableroDeLaVersion() {
        comoUsuario("user");
        when(versionRepository.findTableroIdByVersionId(3L)).thenReturn(Optional.of(1L));
        when(membresiaTableroCacheService.esMiembro("user", 1L)).thenReturn(true);

        assertTrue(guard.puedeAccederAVersion(authentication, 3L));
    }

    @Test
    public void testEstado_NoEncontrado() {
        comoUsuario("user");
        when(estadoTableroRepository.findTableroIdByEstadoId(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> guard.puedeAccederAEstado(authentication, 99L));
    }

    @Test
    public void testVinculo_ResuelveViaTicketOrigen() {
        comoUsuario("user");
        when(ticketVinculoRepository.findTicketOrigenIdById(7L)).thenReturn(Optional.of(5L));
        when(ticketRepository.findTableroIdByTicketId(5L)).thenReturn(Optional.of(1L));
        when(membresiaTableroCacheService.esMiembro("user", 1L)).thenReturn(false);

        assertFalse(guard.puedeAccederAVinculo(authentication, 7L));
    }

    @Test
    public void testReunion_MiembroDelTableroDeLaReunion() {
        comoUsuario("user");
        when(reunionRepository.findTableroIdByReunionId(4L)).thenReturn(Optional.of(1L));
        when(membresiaTableroCacheService.esMiembro("user", 1L)).thenReturn(true);

        assertTrue(guard.puedeAccederAReunion(authentication, 4L));
    }

    @Test
    public void testReunion_NoEncontrada() {
        comoUsuario("user");
        when(reunionRepository.findTableroIdByReunionId(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> guard.puedeAccederAReunion(authentication, 99L));
    }

    // ---- Etiqueta (regla especial: globales solo ADMIN) ----

    @Test
    public void testEtiqueta_GlobalSoloAdmin() {
        comoUsuario("user");
        Etiqueta global = Etiqueta.builder().id(8L).nombre("Global").tablero(null).build();
        when(etiquetaRepository.findById(8L)).thenReturn(Optional.of(global));

        assertFalse(guard.puedeAccederAEtiqueta(authentication, 8L));
    }

    @Test
    public void testEtiqueta_GlobalConAdminPasa() {
        comoAdmin();

        assertTrue(guard.puedeAccederAEtiqueta(authentication, 8L));

        verifyNoInteractions(etiquetaRepository);
    }

    @Test
    public void testEtiqueta_DeTableroAplicaMembresia() {
        comoUsuario("user");
        Tablero tablero = Tablero.builder().id(1L).titulo("T").build();
        Etiqueta deTablero = Etiqueta.builder().id(9L).nombre("Bug").tablero(tablero).build();
        when(etiquetaRepository.findById(9L)).thenReturn(Optional.of(deTablero));
        when(membresiaTableroCacheService.esMiembro("user", 1L)).thenReturn(true);

        assertTrue(guard.puedeAccederAEtiqueta(authentication, 9L));
    }

    @Test
    public void testEtiqueta_NoEncontrada() {
        comoUsuario("user");
        when(etiquetaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> guard.puedeAccederAEtiqueta(authentication, 99L));
    }

    @Test
    public void testCrearEtiqueta_GlobalSoloAdmin() {
        comoUsuario("user");

        assertFalse(guard.puedeCrearEtiqueta(authentication, null));
    }

    @Test
    public void testCrearEtiqueta_EnTableroConMembresia() {
        comoUsuario("user");
        when(membresiaTableroCacheService.esMiembro("user", 1L)).thenReturn(true);

        assertTrue(guard.puedeCrearEtiqueta(authentication, 1L));
    }
}
