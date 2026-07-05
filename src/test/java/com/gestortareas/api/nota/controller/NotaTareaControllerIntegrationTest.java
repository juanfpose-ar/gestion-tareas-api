package com.gestortareas.api.nota.controller;

import tools.jackson.databind.ObjectMapper;
import com.gestortareas.api.nota.dto.NotaTareaDTO;
import com.gestortareas.api.nota.service.NotaTareaService;
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
public class NotaTareaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private NotaTareaService service;

    @Test
    @WithMockUser
    public void testGetByTicket() throws Exception {
        NotaTareaDTO dto = NotaTareaDTO.builder().id(10L).texto("Test note").build();
        when(service.findByTicketId(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/tickets/1/notas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].texto").value("Test note"));
    }

    @Test
    @WithMockUser
    public void testAddNota() throws Exception {
        NotaTareaDTO request = NotaTareaDTO.builder().texto("New note").build();
        NotaTareaDTO dto = NotaTareaDTO.builder().id(11L).texto("New note").build();

        when(service.addNota(eq(1L), any(NotaTareaDTO.class))).thenReturn(dto);

        mockMvc.perform(post("/api/tickets/1/notas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(11));
    }

    @Test
    @WithMockUser
    public void testUpdateNota() throws Exception {
        NotaTareaDTO request = NotaTareaDTO.builder().texto("Updated note").build();
        NotaTareaDTO dto = NotaTareaDTO.builder().id(10L).texto("Updated note").build();

        when(service.updateNota(eq(10L), any(NotaTareaDTO.class))).thenReturn(dto);

        mockMvc.perform(put("/api/tickets/1/notas/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.texto").value("Updated note"));
    }

    @Test
    @WithMockUser
    public void testDeleteNota() throws Exception {
        doNothing().when(service).deleteNota(10L);

        mockMvc.perform(delete("/api/tickets/1/notas/10"))
                .andExpect(status().isNoContent());
    }
}
