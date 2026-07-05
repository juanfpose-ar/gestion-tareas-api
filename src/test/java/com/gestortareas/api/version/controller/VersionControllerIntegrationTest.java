package com.gestortareas.api.version.controller;

import tools.jackson.databind.ObjectMapper;
import com.gestortareas.api.version.dto.VersionDTO;
import com.gestortareas.api.version.dto.VersionRequest;
import com.gestortareas.api.version.service.VersionService;
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
public class VersionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private VersionService versionService;

    @Test
    @WithMockUser
    public void testListarPorTablero() throws Exception {
        VersionDTO dto = VersionDTO.builder().id(10L).titulo("v1.0").build();
        when(versionService.listarPorTablero(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/tableros/1/versiones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].titulo").value("v1.0"));
    }

    @Test
    @WithMockUser
    public void testCrearVersion() throws Exception {
        VersionRequest request = new VersionRequest("v2.0", "2026-12-31", 1L, null, null);
        VersionDTO dto = VersionDTO.builder().id(11L).titulo("v2.0").build();
        when(versionService.crearVersion(any(VersionRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/versiones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.titulo").value("v2.0"));
    }

    @Test
    @WithMockUser
    public void testActualizarVersion() throws Exception {
        VersionRequest request = new VersionRequest("v1.0 Mod", "2026-12-31", 1L, null, null);
        VersionDTO dto = VersionDTO.builder().id(10L).titulo("v1.0 Mod").build();
        when(versionService.actualizarVersion(eq(10L), any(VersionRequest.class))).thenReturn(dto);

        mockMvc.perform(put("/api/versiones/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.titulo").value("v1.0 Mod"));
    }

    @Test
    @WithMockUser
    public void testEliminarVersion() throws Exception {
        doNothing().when(versionService).eliminarVersion(10L);

        mockMvc.perform(delete("/api/versiones/10"))
                .andExpect(status().isNoContent());
    }
}
