package com.gestortareas.api.estado.controller;

import tools.jackson.databind.ObjectMapper;
import com.gestortareas.api.estado.dto.EstadoTableroDTO;
import com.gestortareas.api.estado.dto.EstadoTableroRequest;
import com.gestortareas.api.estado.service.EstadoTableroService;
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
public class EstadoTableroControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private EstadoTableroService estadoService;

    @Test
    @WithMockUser
    public void testListarPorTablero() throws Exception {
        EstadoTableroDTO dto = EstadoTableroDTO.builder().id(10L).nombre("ToDo").build();
        when(estadoService.listarPorTablero(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/estados/tablero/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].nombre").value("ToDo"));
    }

    @Test
    @WithMockUser
    public void testCrearEstado() throws Exception {
        EstadoTableroRequest request = new EstadoTableroRequest();
        request.setNombre("ToDo");
        request.setOrden(1);
        request.setColorHex("#ffffff");
        request.setTableroId(1L);

        EstadoTableroDTO dto = EstadoTableroDTO.builder().id(10L).nombre("ToDo").build();
        when(estadoService.crearEstado(any(EstadoTableroRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/estados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @WithMockUser
    public void testActualizarEstado() throws Exception {
        EstadoTableroRequest request = new EstadoTableroRequest();
        request.setNombre("ToDo Updated");
        request.setOrden(2);

        EstadoTableroDTO dto = EstadoTableroDTO.builder().id(10L).nombre("ToDo Updated").build();
        when(estadoService.actualizarEstado(eq(10L), any(EstadoTableroRequest.class))).thenReturn(dto);

        mockMvc.perform(put("/api/estados/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.nombre").value("ToDo Updated"));
    }

    @Test
    @WithMockUser
    public void testEliminarEstado() throws Exception {
        doNothing().when(estadoService).eliminarEstado(10L);

        mockMvc.perform(delete("/api/estados/10"))
                .andExpect(status().isNoContent());
    }
}
