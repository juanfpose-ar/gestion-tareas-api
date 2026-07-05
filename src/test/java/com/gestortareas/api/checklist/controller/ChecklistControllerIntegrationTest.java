package com.gestortareas.api.checklist.controller;

import tools.jackson.databind.ObjectMapper;
import com.gestortareas.api.checklist.dto.ChecklistItemDTO;
import com.gestortareas.api.checklist.service.ChecklistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpStatus;
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
public class ChecklistControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ChecklistService service;

    @Test
    @WithMockUser
    public void testGetByTicket() throws Exception {
        ChecklistItemDTO dto = ChecklistItemDTO.builder().id(10L).texto("Item").build();
        when(service.findByTicketId(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/tickets/1/checklist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].texto").value("Item"));
    }

    @Test
    @WithMockUser
    public void testAddItem() throws Exception {
        ChecklistItemDTO request = ChecklistItemDTO.builder().texto("New Item").build();
        ChecklistItemDTO dto = ChecklistItemDTO.builder().id(11L).texto("New Item").build();

        when(service.addItem(eq(1L), any(ChecklistItemDTO.class))).thenReturn(dto);

        mockMvc.perform(post("/api/tickets/1/checklist")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(11));
    }

    @Test
    @WithMockUser
    public void testUpdateItem() throws Exception {
        ChecklistItemDTO request = ChecklistItemDTO.builder().texto("Updated Item").build();
        ChecklistItemDTO dto = ChecklistItemDTO.builder().id(10L).texto("Updated Item").build();

        when(service.updateItem(eq(10L), any(ChecklistItemDTO.class))).thenReturn(dto);

        mockMvc.perform(put("/api/tickets/1/checklist/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.texto").value("Updated Item"));
    }

    @Test
    @WithMockUser
    public void testDeleteItem() throws Exception {
        doNothing().when(service).deleteItem(10L);

        mockMvc.perform(delete("/api/tickets/1/checklist/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    public void testToggleItem() throws Exception {
        ChecklistItemDTO dto = ChecklistItemDTO.builder().id(10L).completado(true).build();
        when(service.toggleItem(10L)).thenReturn(dto);

        mockMvc.perform(put("/api/tickets/1/checklist/10/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completado").value(true));
    }

    @Test
    @WithMockUser
    public void testReordenar() throws Exception {
        doNothing().when(service).reordenar(eq(1L), any(List.class));

        mockMvc.perform(put("/api/tickets/1/checklist/reordenar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[10, 11]"))
                .andExpect(status().isNoContent());
    }
}
