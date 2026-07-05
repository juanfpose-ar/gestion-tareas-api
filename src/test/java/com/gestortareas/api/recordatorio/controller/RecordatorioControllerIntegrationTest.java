package com.gestortareas.api.recordatorio.controller;

import tools.jackson.databind.ObjectMapper;
import com.gestortareas.api.enums.TipoRecordatorio;
import com.gestortareas.api.recordatorio.dto.RecordatorioDTO;
import com.gestortareas.api.recordatorio.service.RecordatorioService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class RecordatorioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private RecordatorioService service;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetByTicket() throws Exception {
        RecordatorioDTO dto = RecordatorioDTO.builder().id(10L).tipo(TipoRecordatorio.PERSONALIZADO).build();
        when(service.findByTicketId(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/tickets/1/recordatorios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testAddRecordatorio() throws Exception {
        RecordatorioDTO request = RecordatorioDTO.builder().tipo(TipoRecordatorio.PERSONALIZADO).build();
        RecordatorioDTO dto = RecordatorioDTO.builder().id(11L).tipo(TipoRecordatorio.PERSONALIZADO).build();

        when(service.addRecordatorio(eq(1L), any(RecordatorioDTO.class))).thenReturn(dto);

        mockMvc.perform(post("/api/tickets/1/recordatorios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(11));
    }
}
