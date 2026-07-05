package com.gestortareas.api.etiqueta.controller;

import tools.jackson.databind.ObjectMapper;
import com.gestortareas.api.etiqueta.dto.EtiquetaDTO;
import com.gestortareas.api.etiqueta.dto.EtiquetaRequest;
import com.gestortareas.api.etiqueta.service.EtiquetaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class EtiquetaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private EtiquetaService etiquetaService;

    @Test
    @WithMockUser
    public void testListarPorTablero() throws Exception {
        EtiquetaDTO dto = EtiquetaDTO.builder().id(10L).nombre("Bug").build();
        when(etiquetaService.listarPorTableroOGlobales(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/etiquetas/tablero/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].nombre").value("Bug"));
    }

    @Test
    @WithMockUser
    public void testListarGlobales() throws Exception {
        EtiquetaDTO dto = EtiquetaDTO.builder().id(10L).nombre("Bug").build();
        when(etiquetaService.listarGlobales()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/etiquetas/globales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    @WithMockUser
    public void testCrearEtiqueta() throws Exception {
        EtiquetaRequest request = new EtiquetaRequest();
        request.setNombre("Feature");
        request.setColor("#ffffff");
        request.setColorTexto("#000000");
        request.setTableroId(1L);

        EtiquetaDTO dto = EtiquetaDTO.builder().id(10L).nombre("Feature").build();
        when(etiquetaService.crearEtiqueta(any(EtiquetaRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/etiquetas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @WithMockUser
    public void testActualizarEtiqueta() throws Exception {
        EtiquetaRequest request = new EtiquetaRequest();
        request.setNombre("Feature Updated");
        request.setColor("#ffffff");
        request.setColorTexto("#000000");

        EtiquetaDTO dto = EtiquetaDTO.builder().id(10L).nombre("Feature Updated").build();
        when(etiquetaService.actualizarEtiqueta(eq(10L), any(EtiquetaRequest.class))).thenReturn(dto);

        mockMvc.perform(put("/api/etiquetas/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.nombre").value("Feature Updated"));
    }

    @Test
    @WithMockUser
    public void testEliminarEtiqueta() throws Exception {
        doNothing().when(etiquetaService).eliminarEtiqueta(10L);

        mockMvc.perform(delete("/api/etiquetas/10"))
                .andExpect(status().isNoContent());
    }
}
