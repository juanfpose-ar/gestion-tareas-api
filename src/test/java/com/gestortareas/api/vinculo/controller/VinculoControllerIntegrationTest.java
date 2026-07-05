package com.gestortareas.api.vinculo.controller;

import tools.jackson.databind.ObjectMapper;
import com.gestortareas.api.vinculo.dto.VinculoDTO;
import com.gestortareas.api.vinculo.dto.VinculoRequest;
import com.gestortareas.api.vinculo.service.VinculoService;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class VinculoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private VinculoService vinculoService;

    @Test
    @WithMockUser
    public void testCrearVinculo() throws Exception {
        VinculoRequest request = new VinculoRequest();
        request.setTicketOrigenId(1L);
        request.setTicketDestinoId(2L);
        request.setTipoVinculo("BLOQUEA");

        VinculoDTO dto = VinculoDTO.builder().id(10L).tipoVinculo("BLOQUEA").build();

        when(vinculoService.crearVinculo(any(VinculoRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/vinculos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @WithMockUser
    public void testObtenerVinculosPorTicket() throws Exception {
        VinculoDTO dto = VinculoDTO.builder().id(10L).tipoVinculo("BLOQUEA").build();
        when(vinculoService.obtenerVinculosPorTicket(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/vinculos/ticket/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));
    }
}
