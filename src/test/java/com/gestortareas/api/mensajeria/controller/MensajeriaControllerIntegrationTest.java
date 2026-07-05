package com.gestortareas.api.mensajeria.controller;

import tools.jackson.databind.ObjectMapper;
import com.gestortareas.api.mensajeria.dto.*;
import com.gestortareas.api.mensajeria.service.MensajeriaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class MensajeriaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private MensajeriaService mensajeriaService;

    @Test
    @WithMockUser(username = "emisor")
    public void testObtenerBandeja_Retorna200() throws Exception {
        ConversacionResumenDTO resumen = new ConversacionResumenDTO(
                1L,
                "Asunto Test",
                1L,
                "Emisor",
                "Mensaje",
                1,
                false,
                LocalDateTime.now(),
                false,
                "Destinatario"
        );

        when(mensajeriaService.obtenerBandeja("emisor")).thenReturn(List.of(resumen));

        mockMvc.perform(get("/api/mensajeria/bandeja"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].asunto").value("Asunto Test"))
                .andExpect(jsonPath("$[0].tieneNoLeidos").value(false));
    }

    @Test
    @WithMockUser(username = "emisor")
    public void testObtenerConversacion_Retorna200() throws Exception {
        ConversacionDetalleDTO detalle = new ConversacionDetalleDTO(
                1L,
                "Asunto Test",
                LocalDateTime.now(),
                List.of(new MensajeDTO(1L, 1L, "Emisor", "Mensaje", LocalDateTime.now())),
                List.of(1L, 2L)
        );

        when(mensajeriaService.obtenerConversacion(eq(1L), eq("emisor"))).thenReturn(detalle);

        mockMvc.perform(get("/api/mensajeria/conversaciones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.asunto").value("Asunto Test"));
    }

    @Test
    @WithMockUser(username = "emisor")
    public void testCrearConversacion_Retorna201() throws Exception {
        NuevaConversacionRequest request = new NuevaConversacionRequest("Asunto Test", "Contenido", List.of(2L));
        ConversacionResumenDTO resumen = new ConversacionResumenDTO(
                1L,
                "Asunto Test",
                1L,
                "Emisor",
                "Contenido",
                1,
                false,
                LocalDateTime.now(),
                false,
                "Destinatario"
        );

        when(mensajeriaService.crearConversacion(any(NuevaConversacionRequest.class), eq("emisor"))).thenReturn(resumen);

        mockMvc.perform(post("/api/mensajeria/conversaciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.asunto").value("Asunto Test"));
    }

    @Test
    @WithMockUser(username = "emisor")
    public void testResponderConversacion_Retorna201() throws Exception {
        ResponderConversacionRequest request = new ResponderConversacionRequest("Respuesta");
        ConversacionDetalleDTO detalle = new ConversacionDetalleDTO(
                1L,
                "Asunto Test",
                LocalDateTime.now(),
                List.of(new MensajeDTO(1L, 1L, "Emisor", "Respuesta", LocalDateTime.now())),
                List.of(1L, 2L)
        );

        when(mensajeriaService.responderConversacion(eq(1L), any(ResponderConversacionRequest.class), eq("emisor")))
                .thenReturn(detalle);

        mockMvc.perform(post("/api/mensajeria/conversaciones/1/responder")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "emisor")
    public void testActualizarEstado_Retorna204() throws Exception {
        ActualizarEstadoRequest request = new ActualizarEstadoRequest(true, false);

        mockMvc.perform(patch("/api/mensajeria/conversaciones/1/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }
}
