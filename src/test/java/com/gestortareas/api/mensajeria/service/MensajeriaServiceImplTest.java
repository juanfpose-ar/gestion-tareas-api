package com.gestortareas.api.mensajeria.service;

import com.gestortareas.api.exceptions.BusinessValidationException;
import com.gestortareas.api.mensajeria.dto.*;
import com.gestortareas.api.mensajeria.entity.*;
import com.gestortareas.api.mensajeria.repository.*;
import com.gestortareas.api.usuario.entity.Usuario;
import com.gestortareas.api.usuario.repository.UsuarioRepository;
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

    @BeforeEach
    public void setup() {
        userEmisor = Usuario.builder()
                .id(1L)
                .username("emisor")
                .nombreCompleto("Emisor Test")
                .build();

        userReceptor = Usuario.builder()
                .id(2L)
                .username("receptor")
                .nombreCompleto("Receptor Test")
                .build();
    }

    @Test
    public void testCrearConversacion_Exitoso() {
        NuevaConversacionRequest request = new NuevaConversacionRequest("Asunto Test", "Contenido inicial", List.of(2L));

        Conversacion conversacion = Conversacion.builder()
                .id(10L)
                .asunto("Asunto Test")
                .fechaUltimaActividad(LocalDateTime.now())
                .build();

        when(usuarioRepository.findByUsername("emisor")).thenReturn(Optional.of(userEmisor));
        when(conversacionRepository.save(any(Conversacion.class))).thenReturn(conversacion);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(userReceptor));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(userEmisor));
        when(usuarioConversacionRepository.save(any(UsuarioConversacion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConversacionResumenDTO result = mensajeriaService.crearConversacion(request, "emisor");

        assertNotNull(result);
        assertEquals("Asunto Test", result.asunto());
        assertFalse(result.tieneNoLeidos());
        verify(mensajeRepository, times(1)).save(any(Mensaje.class));
        verify(usuarioConversacionRepository, times(2)).save(any(UsuarioConversacion.class));
    }

    @Test
    public void testObtenerConversacion_MarcaLeida() {
        Conversacion conversacion = Conversacion.builder()
                .id(10L)
                .asunto("Asunto Test")
                .fechaUltimaActividad(LocalDateTime.now())
                .participantes(new ArrayList<>())
                .build();

        UsuarioConversacion uc = UsuarioConversacion.builder()
                .id(1L)
                .usuario(userEmisor)
                .conversacion(conversacion)
                .leida(false)
                .build();

        when(usuarioRepository.findByUsername("emisor")).thenReturn(Optional.of(userEmisor));
        when(conversacionRepository.findById(10L)).thenReturn(Optional.of(conversacion));
        when(usuarioConversacionRepository.findByUsuarioIdAndConversacionId(1L, 10L)).thenReturn(Optional.of(uc));
        when(mensajeRepository.findByConversacionIdOrderByFechaEnvioAsc(10L)).thenReturn(List.of());

        ConversacionDetalleDTO result = mensajeriaService.obtenerConversacion(10L, "emisor");

        assertNotNull(result);
        assertTrue(uc.isLeida()); // Verificamos que se haya marcado como leída
        verify(usuarioConversacionRepository, times(1)).save(uc);
    }

    @Test
    public void testResponderConversacion_LanzaExcepcionSiEstaEliminada() {
        ResponderConversacionRequest request = new ResponderConversacionRequest("Respuesta");

        Conversacion conversacion = Conversacion.builder()
                .id(10L)
                .asunto("Asunto Test")
                .build();

        UsuarioConversacion uc = UsuarioConversacion.builder()
                .id(1L)
                .usuario(userEmisor)
                .conversacion(conversacion)
                .eliminada(true) // Conversación eliminada para este usuario
                .build();

        when(usuarioRepository.findByUsername("emisor")).thenReturn(Optional.of(userEmisor));
        when(conversacionRepository.findById(10L)).thenReturn(Optional.of(conversacion));
        when(usuarioConversacionRepository.findByUsuarioIdAndConversacionId(1L, 10L)).thenReturn(Optional.of(uc));

        assertThrows(BusinessValidationException.class, () -> {
            mensajeriaService.responderConversacion(10L, request, "emisor");
        });

        verify(mensajeRepository, never()).save(any(Mensaje.class));
    }

    @Test
    public void testActualizarEstado_Exitoso() {
        ActualizarEstadoRequest request = new ActualizarEstadoRequest(true, false);

        Conversacion conversacion = Conversacion.builder()
                .id(10L)
                .build();

        UsuarioConversacion uc = UsuarioConversacion.builder()
                .id(1L)
                .usuario(userEmisor)
                .conversacion(conversacion)
                .archivada(false)
                .build();

        when(usuarioRepository.findByUsername("emisor")).thenReturn(Optional.of(userEmisor));
        when(usuarioConversacionRepository.findByUsuarioIdAndConversacionId(1L, 10L)).thenReturn(Optional.of(uc));

        mensajeriaService.actualizarEstado(10L, request, "emisor");

        assertTrue(uc.isArchivada());
        assertFalse(uc.isEliminada());
        verify(usuarioConversacionRepository, times(1)).save(uc);
    }
}
