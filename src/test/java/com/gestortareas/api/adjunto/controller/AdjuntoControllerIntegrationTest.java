package com.gestortareas.api.adjunto.controller;

import tools.jackson.databind.ObjectMapper;
import com.gestortareas.api.adjunto.dto.AdjuntoDTO;
import com.gestortareas.api.adjunto.dto.EnlaceRequest;
import com.gestortareas.api.adjunto.service.AdjuntoService;
import com.gestortareas.api.enums.TipoAdjunto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class AdjuntoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AdjuntoService service;

    @Test
    @WithMockUser
    public void testGetByTicket() throws Exception {
        AdjuntoDTO dto = AdjuntoDTO.builder().id(10L).nombre("test.txt").tipo(TipoAdjunto.ARCHIVO).build();
        when(service.findByTicketId(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/tickets/1/adjuntos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].nombre").value("test.txt"));
    }

    @Test
    @WithMockUser
    public void testUploadArchivo() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        AdjuntoDTO dto = AdjuntoDTO.builder().id(10L).nombre("test.txt").tipo(TipoAdjunto.ARCHIVO).build();

        when(service.uploadArchivo(eq(1L), any(MultipartFile.class))).thenReturn(dto);

        mockMvc.perform(multipart("/api/tickets/1/adjuntos/archivo")
                .file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @WithMockUser
    public void testAddEnlace() throws Exception {
        EnlaceRequest request = new EnlaceRequest("http://google.com", "Google");
        AdjuntoDTO dto = AdjuntoDTO.builder().id(11L).nombre("Google").tipo(TipoAdjunto.ENLACE).build();

        when(service.addEnlace(eq(1L), any(EnlaceRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/tickets/1/adjuntos/enlace")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(11));
    }

    @Test
    @WithMockUser
    public void testDeleteAdjunto() throws Exception {
        doNothing().when(service).deleteAdjunto(10L);

        mockMvc.perform(delete("/api/tickets/1/adjuntos/10"))
                .andExpect(status().isNoContent());
    }
}
