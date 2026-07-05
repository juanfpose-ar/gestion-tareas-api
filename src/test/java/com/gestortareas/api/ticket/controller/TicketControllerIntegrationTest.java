package com.gestortareas.api.ticket.controller;

import tools.jackson.databind.ObjectMapper;
import com.gestortareas.api.enums.Prioridad;
import com.gestortareas.api.ticket.dto.TicketCardDTO;
import com.gestortareas.api.ticket.dto.TicketDTO;
import com.gestortareas.api.ticket.dto.TicketRequest;
import com.gestortareas.api.ticket.service.TicketService;
import com.gestortareas.api.usuario.entity.Usuario;
import com.gestortareas.api.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class TicketControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private TicketService ticketService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    @WithMockUser
    public void testListarActivosPorTablero() throws Exception {
        TicketCardDTO card = TicketCardDTO.builder().id(10L).titulo("Test Card").build();
        when(ticketService.listarActivosPorTablero(1L)).thenReturn(List.of(card));

        mockMvc.perform(get("/api/tickets/tablero/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].titulo").value("Test Card"));
    }

    @Test
    @WithMockUser
    public void testListarArchivadosPorTablero() throws Exception {
        TicketCardDTO card = TicketCardDTO.builder().id(10L).titulo("Archived Card").build();
        when(ticketService.listarArchivadosPorTablero(1L)).thenReturn(List.of(card));

        mockMvc.perform(get("/api/tickets/tablero/1/archivados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    @WithMockUser
    public void testListarPorVersion() throws Exception {
        TicketCardDTO card = TicketCardDTO.builder().id(10L).titulo("Version Card").build();
        when(ticketService.listarPorVersion(2L)).thenReturn(List.of(card));

        mockMvc.perform(get("/api/tickets/version/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    @WithMockUser
    public void testObtenerPorId() throws Exception {
        TicketDTO dto = TicketDTO.builder().id(10L).titulo("Ticket").build();
        when(ticketService.obtenerPorId(10L)).thenReturn(dto);

        mockMvc.perform(get("/api/tickets/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @WithMockUser(username = "admin")
    public void testCrearTicket_Retorna201() throws Exception {
        TicketRequest request = new TicketRequest();
        request.setTitulo("New Ticket");
        request.setDescripcion("Desc");
        request.setPrioridad(Prioridad.ALTA);
        request.setEstadoId(1L);

        TicketDTO dto = TicketDTO.builder().id(20L).titulo("New Ticket").build();

        Usuario admin = Usuario.builder().id(1L).username("admin").build();
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(ticketService.crearTicket(any(TicketRequest.class), eq(1L))).thenReturn(dto);

        mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(20));
    }

    @Test
    @WithMockUser(username = "admin")
    public void testActualizarTicket() throws Exception {
        TicketRequest request = new TicketRequest();
        request.setTitulo("Updated Ticket");
        request.setPrioridad(Prioridad.ALTA);
        request.setEstadoId(1L);

        TicketDTO dto = TicketDTO.builder().id(10L).titulo("Updated Ticket").build();

        Usuario admin = Usuario.builder().id(1L).username("admin").build();
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(ticketService.actualizarTicket(eq(10L), any(TicketRequest.class), eq(1L))).thenReturn(dto);

        mockMvc.perform(put("/api/tickets/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Updated Ticket"));
    }

    @Test
    @WithMockUser
    public void testCambiarEstado() throws Exception {
        TicketDTO dto = TicketDTO.builder().id(10L).estadoId(2L).build();
        when(ticketService.cambiarEstado(10L, 2L)).thenReturn(dto);

        mockMvc.perform(patch("/api/tickets/10/estado")
                .param("nuevoEstadoId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoId").value(2));
    }

    @Test
    @WithMockUser
    public void testReordenarEnEstado() throws Exception {
        doNothing().when(ticketService).reordenarEnEstado(eq(1L), any(List.class));

        mockMvc.perform(put("/api/tickets/estado/1/reordenar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[20, 10]"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    public void testCambiarArchivado() throws Exception {
        TicketDTO dto = TicketDTO.builder().id(10L).archivado(true).build();
        when(ticketService.cambiarArchivado(10L, true)).thenReturn(dto);

        mockMvc.perform(patch("/api/tickets/10/archivar")
                .param("archivado", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.archivado").value(true));
    }

    @Test
    @WithMockUser
    public void testCambiarCompletado() throws Exception {
        TicketDTO dto = TicketDTO.builder().id(10L).completado(true).build();
        when(ticketService.cambiarCompletado(10L, true)).thenReturn(dto);

        mockMvc.perform(patch("/api/tickets/10/completado")
                .param("completado", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completado").value(true));
    }

    @Test
    @WithMockUser
    public void testEliminarTicket() throws Exception {
        doNothing().when(ticketService).eliminarTicket(10L);

        mockMvc.perform(delete("/api/tickets/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    public void testAsignarUsuario() throws Exception {
        TicketDTO dto = TicketDTO.builder().id(10L).build();
        when(ticketService.asignarUsuario(10L, 5L)).thenReturn(dto);

        mockMvc.perform(post("/api/tickets/10/asignados/5"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void testDesasignarUsuario() throws Exception {
        TicketDTO dto = TicketDTO.builder().id(10L).build();
        when(ticketService.desasignarUsuario(10L, 5L)).thenReturn(dto);

        mockMvc.perform(delete("/api/tickets/10/asignados/5"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void testAsignarInformado() throws Exception {
        TicketDTO dto = TicketDTO.builder().id(10L).build();
        when(ticketService.asignarInformado(10L, 5L)).thenReturn(dto);

        mockMvc.perform(post("/api/tickets/10/informados/5"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void testDesasignarInformado() throws Exception {
        TicketDTO dto = TicketDTO.builder().id(10L).build();
        when(ticketService.desasignarInformado(10L, 5L)).thenReturn(dto);

        mockMvc.perform(delete("/api/tickets/10/informados/5"))
                .andExpect(status().isOk());
    }
}
