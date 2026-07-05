package com.gestortareas.api.mensajeria.service;

import com.gestortareas.api.exceptions.BusinessValidationException;
import com.gestortareas.api.mensajeria.dto.*;
import com.gestortareas.api.mensajeria.entity.*;
import com.gestortareas.api.mensajeria.repository.*;
import com.gestortareas.api.usuario.entity.Usuario;
import com.gestortareas.api.usuario.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MensajeriaServiceImplTest {

    @Mock
    private ConversacionRepository conversacionRepository;

    @Mock
    private MensajeRepository mensajeRepository;

    @Mock
    private UsuarioConversacionRepository usuarioConversacionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private MensajeriaServiceImpl mensajeriaService;

    private Usuario userEmisor;
    private Usuario userReceptor;
    private Conversacion conversacion;
    private UsuarioConversacion ucEmisor;

    @BeforeEach
    public void setup() {
        userEmisor = Usuario.builder().id(1L).username("emisor").nombreCompleto("Emisor Test").build();
        userReceptor = Usuario.builder().id(2L).username("receptor").nombreCompleto("Receptor Test").build();
        conversacion = Conversacion.builder().id(10L).asunto("Asunto Test").fechaUltimaActividad(LocalDateTime.now())
                .participantes(new ArrayList<>()).build();
        ucEmisor = UsuarioConversacion.builder().id(1L).usuario(userEmisor).conversacion(conversacion).leida(false)
                .archivada(false).eliminada(false).destacada(false).build();
        conversacion.getParticipantes().add(ucEmisor);
    }

    @Test
    public void testObtenerBandeja_Exitoso() {
        when(usuarioRepository.findByUsername("emisor")).thenReturn(Optional.of(userEmisor));
        when(usuarioConversacionRepository.findActiveConversationsForUser(1L)).thenReturn(List.of(ucEmisor));

        Mensaje msg = Mensaje.builder().id(100L).emisor(userEmisor).contenido("Hola").fechaEnvio(LocalDateTime.now())
                .build();
        conversacion.setMensajes(List.of(msg));
        when(mensajeRepository.findFirstByConversacionIdOrderByFechaEnvioDesc(10L)).thenReturn(msg);

        List<ConversacionResumenDTO> result = mensajeriaService.obtenerBandeja("emisor");

        assertEquals(1, result.size());
        assertEquals("Yo", result.get(0).nombresParticipantes()); // only emisor is participant, so "Yo"
    }

    @Test
    public void testObtenerBandeja_UsuarioNoEncontrado() {
        when(usuarioRepository.findByUsername("emisor")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> mensajeriaService.obtenerBandeja("emisor"));
    }

    @Test
    public void testObtenerConversacion_Exitoso() {
        when(usuarioRepository.findByUsername("emisor")).thenReturn(Optional.of(userEmisor));
        when(conversacionRepository.findById(10L)).thenReturn(Optional.of(conversacion));
        when(usuarioConversacionRepository.findByUsuarioIdAndConversacionId(1L, 10L)).thenReturn(Optional.of(ucEmisor));

        Mensaje msg = Mensaje.builder().id(100L).emisor(userEmisor).contenido("Hola").fechaEnvio(LocalDateTime.now())
                .build();
        when(mensajeRepository.findByConversacionIdOrderByFechaEnvioAsc(10L)).thenReturn(List.of(msg));

        ConversacionDetalleDTO result = mensajeriaService.obtenerConversacion(10L, "emisor");

        assertNotNull(result);
        assertTrue(ucEmisor.isLeida());
    }

    @Test
    public void testObtenerConversacion_NoParticipa() {
        when(usuarioRepository.findByUsername("emisor")).thenReturn(Optional.of(userEmisor));
        when(conversacionRepository.findById(10L)).thenReturn(Optional.of(conversacion));
        when(usuarioConversacionRepository.findByUsuarioIdAndConversacionId(1L, 10L)).thenReturn(Optional.empty());

        assertThrows(BusinessValidationException.class, () -> mensajeriaService.obtenerConversacion(10L, "emisor"));
    }

    @Test
    public void testObtenerConversacion_Eliminada() {
        ucEmisor.setEliminada(true);
        when(usuarioRepository.findByUsername("emisor")).thenReturn(Optional.of(userEmisor));
        when(conversacionRepository.findById(10L)).thenReturn(Optional.of(conversacion));
        when(usuarioConversacionRepository.findByUsuarioIdAndConversacionId(1L, 10L)).thenReturn(Optional.of(ucEmisor));

        assertThrows(BusinessValidationException.class, () -> mensajeriaService.obtenerConversacion(10L, "emisor"));
    }

    @Test
    public void testCrearConversacion_Exitoso() {
        NuevaConversacionRequest request = new NuevaConversacionRequest("Asunto Test", "Contenido", List.of(2L));

        when(usuarioRepository.findByUsername("emisor")).thenReturn(Optional.of(userEmisor));
        when(conversacionRepository.save(any(Conversacion.class))).thenReturn(conversacion);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(userReceptor));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(userEmisor));
        when(usuarioConversacionRepository.save(any(UsuarioConversacion.class))).thenAnswer(inv -> inv.getArgument(0));

        ConversacionResumenDTO result = mensajeriaService.crearConversacion(request, "emisor");

        assertNotNull(result);
        assertEquals("Asunto Test", result.asunto());
        verify(mensajeRepository, times(1)).save(any(Mensaje.class));
    }

    @Test
    public void testResponderConversacion_Exitoso() {
        ResponderConversacionRequest request = new ResponderConversacionRequest("Respuesta");

        when(usuarioRepository.findByUsername("emisor")).thenReturn(Optional.of(userEmisor));
        when(conversacionRepository.findById(10L)).thenReturn(Optional.of(conversacion));
        when(usuarioConversacionRepository.findByUsuarioIdAndConversacionId(1L, 10L)).thenReturn(Optional.of(ucEmisor));

        ConversacionDetalleDTO result = mensajeriaService.responderConversacion(10L, request, "emisor");

        assertNotNull(result);
        verify(mensajeRepository, times(1)).save(any(Mensaje.class));
    }

    @Test
    public void testResponderConversacion_NoParticipa() {
        ResponderConversacionRequest request = new ResponderConversacionRequest("Respuesta");

        when(usuarioRepository.findByUsername("emisor")).thenReturn(Optional.of(userEmisor));
        when(conversacionRepository.findById(10L)).thenReturn(Optional.of(conversacion));
        when(usuarioConversacionRepository.findByUsuarioIdAndConversacionId(1L, 10L)).thenReturn(Optional.empty());

        assertThrows(BusinessValidationException.class,
                () -> mensajeriaService.responderConversacion(10L, request, "emisor"));
    }

    @Test
    public void testResponderConversacion_Eliminada() {
        ResponderConversacionRequest request = new ResponderConversacionRequest("Respuesta");
        ucEmisor.setEliminada(true);

        when(usuarioRepository.findByUsername("emisor")).thenReturn(Optional.of(userEmisor));
        when(conversacionRepository.findById(10L)).thenReturn(Optional.of(conversacion));
        when(usuarioConversacionRepository.findByUsuarioIdAndConversacionId(1L, 10L)).thenReturn(Optional.of(ucEmisor));

        assertThrows(BusinessValidationException.class,
                () -> mensajeriaService.responderConversacion(10L, request, "emisor"));
    }

    @Test
    public void testActualizarEstado_Exitoso() {
        ActualizarEstadoRequest request = new ActualizarEstadoRequest(true, true, true, true);

        when(usuarioRepository.findByUsername("emisor")).thenReturn(Optional.of(userEmisor));
        when(usuarioConversacionRepository.findByUsuarioIdAndConversacionId(1L, 10L)).thenReturn(Optional.of(ucEmisor));

        mensajeriaService.actualizarEstado(10L, request, "emisor");

        assertTrue(ucEmisor.isLeida());
        assertTrue(ucEmisor.isArchivada());
        assertTrue(ucEmisor.isEliminada());
        assertTrue(ucEmisor.isDestacada());
        verify(usuarioConversacionRepository, times(1)).save(ucEmisor);
    }
}
