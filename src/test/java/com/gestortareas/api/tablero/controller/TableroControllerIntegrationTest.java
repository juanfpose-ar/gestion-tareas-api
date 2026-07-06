package com.gestortareas.api.tablero.controller;

import tools.jackson.databind.ObjectMapper;
import com.gestortareas.api.tablero.dto.TableroDTO;
import com.gestortareas.api.tablero.dto.TableroRequest;
import com.gestortareas.api.tablero.service.TableroService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class TableroControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private TableroService tableroService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testListarVisibles() throws Exception {
        TableroDTO dto = TableroDTO.builder().id(10L).titulo("Work Board").build();
        when(tableroService.listarVisiblesParaUsuario(eq("admin"), eq(true))).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/tableros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].titulo").value("Work Board"));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    public void testListarTodos_Forbidden() throws Exception {
        mockMvc.perform(get("/api/tableros/admin/todos"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testListarTodos_Admin() throws Exception {
        TableroDTO dto = TableroDTO.builder().id(10L).titulo("Work Board").build();
        when(tableroService.listarTodosParaAdmin()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/tableros/admin/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerPorId() throws Exception {
        TableroDTO dto = TableroDTO.builder().id(10L).titulo("Work Board").build();
        when(tableroService.obtenerPorId(10L)).thenReturn(dto);

        mockMvc.perform(get("/api/tableros/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testCrearTablero() throws Exception {
        TableroRequest request = new TableroRequest();
        request.setTitulo("New Board");
        request.setDescripcion("Desc");
        request.setImagenFondoUrl("url");

        TableroDTO dto = TableroDTO.builder().id(11L).titulo("New Board").build();
        when(tableroService.crearTablero(any(TableroRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/tableros")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(11));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testActualizarTablero() throws Exception {
        TableroRequest request = new TableroRequest();
        request.setTitulo("New Board");
        request.setDescripcion("Desc");
        request.setImagenFondoUrl("url");

        TableroDTO dto = TableroDTO.builder().id(10L).titulo("Updated").build();
        when(tableroService.actualizarTablero(eq(10L), any(TableroRequest.class))).thenReturn(dto);

        mockMvc.perform(put("/api/tableros/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Updated"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testArchivarTablero() throws Exception {
        TableroDTO dto = TableroDTO.builder().id(10L).titulo("Archived").archivado(true).build();
        when(tableroService.archivarTablero(10L, true)).thenReturn(dto);

        mockMvc.perform(patch("/api/tableros/10/archivar")
                .param("archivado", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.archivado").value(true));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testEliminarTablero() throws Exception {
        doNothing().when(tableroService).eliminarTablero(10L);

        mockMvc.perform(delete("/api/tableros/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetMiembros() throws Exception {
        Usuario u = Usuario.builder().id(5L).username("member").nombreCompleto("Member").build();
        when(usuarioRepository.findByTablerosAsignadosId(10L)).thenReturn(List.of(u));

        mockMvc.perform(get("/api/tableros/10/miembros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5));
    }
}
