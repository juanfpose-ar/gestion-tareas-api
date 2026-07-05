package com.gestortareas.api.reunion.controller;

import tools.jackson.databind.ObjectMapper;
import com.gestortareas.api.reunion.dto.ReunionDTO;
import com.gestortareas.api.reunion.dto.ReunionRequest;
import com.gestortareas.api.reunion.service.ReunionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class ReunionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ReunionService reunionService;

    @Test
    @WithMockUser
    public void testListarPorTablero() throws Exception {
        ReunionDTO dto = ReunionDTO.builder().id(10L).titulo("Daily").build();
        when(reunionService.findByTableroId(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/tableros/1/reuniones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].titulo").value("Daily"));
    }

    @Test
    @WithMockUser
    public void testCrear() throws Exception {
        ReunionRequest request = new ReunionRequest();
        request.setTitulo("Planning");
        request.setDescripcion("Desc");
        request.setFecha(LocalDate.now());
        request.setHoraInicio("10:00");
        request.setHoraFin("11:00");
        request.setColor("#0000ff");
        request.setRecordatorioMinutos(30);
        request.setTableroId(1L);

        ReunionDTO dto = ReunionDTO.builder().id(11L).titulo("Planning").build();
        when(reunionService.crear(any(ReunionRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/reuniones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(11));
    }

    @Test
    @WithMockUser
    public void testActualizar() throws Exception {
        ReunionRequest request = new ReunionRequest();
        request.setTitulo("Daily Updated");
        request.setFecha(LocalDate.now());
        request.setHoraInicio("10:00");
        request.setHoraFin("11:00");
        request.setTableroId(1L);

        ReunionDTO dto = ReunionDTO.builder().id(10L).titulo("Daily Updated").build();
        when(reunionService.actualizar(eq(10L), any(ReunionRequest.class))).thenReturn(dto);

        mockMvc.perform(put("/api/reuniones/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.titulo").value("Daily Updated"));
    }

    @Test
    @WithMockUser
    public void testEliminar() throws Exception {
        doNothing().when(reunionService).eliminar(10L);

        mockMvc.perform(delete("/api/reuniones/10"))
                .andExpect(status().isNoContent());
    }
}
