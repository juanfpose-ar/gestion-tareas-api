package com.gestortareas.api.usuario.controller;

import tools.jackson.databind.ObjectMapper;
import com.gestortareas.api.enums.Rol;
import com.gestortareas.api.usuario.dto.BlanqueoPasswordRequest;
import com.gestortareas.api.usuario.dto.UsuarioDTO;
import com.gestortareas.api.usuario.dto.UsuarioRequest;
import com.gestortareas.api.usuario.service.UsuarioService;
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
public class UsuarioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UsuarioService usuarioService;

    @Test
    @WithMockUser
    public void testListarTodos() throws Exception {
        UsuarioDTO dto = UsuarioDTO.builder().id(10L).username("test.user").build();
        when(usuarioService.listarTodos(false)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].username").value("test.user"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerPorId() throws Exception {
        UsuarioDTO dto = UsuarioDTO.builder().id(10L).username("test.user").build();
        when(usuarioService.obtenerPorId(10L)).thenReturn(dto);

        mockMvc.perform(get("/api/usuarios/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCrearUsuario() throws Exception {
        UsuarioRequest request = new UsuarioRequest();
        request.setUsername("new.user");
        request.setPassword("pwd");
        request.setNombreCompleto("New User");
        request.setActivo(true);
        request.setEmail("new@mail.com");
        request.setColorAvatar("#ffffff");
        request.setRol(Rol.USER);

        UsuarioDTO dto = UsuarioDTO.builder().id(11L).username("new.user").build();

        when(usuarioService.crearUsuario(any(UsuarioRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(11));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testActualizarUsuario() throws Exception {
        UsuarioRequest request = new UsuarioRequest();
        request.setUsername("updated.user");
        request.setNombreCompleto("Updated User");
        request.setActivo(true);
        request.setRol(Rol.USER);

        UsuarioDTO dto = UsuarioDTO.builder().id(10L).username("updated.user").build();
        when(usuarioService.actualizarUsuario(eq(10L), any(UsuarioRequest.class))).thenReturn(dto);

        mockMvc.perform(put("/api/usuarios/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.username").value("updated.user"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCambiarEstadoActivo() throws Exception {
        doNothing().when(usuarioService).cambiarEstadoActivo(10L, false);

        mockMvc.perform(patch("/api/usuarios/10/activo")
                .param("activo", "false"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testBlanquearPassword() throws Exception {
        BlanqueoPasswordRequest request = new BlanqueoPasswordRequest();
        request.setPasswordNueva("NewPwd123!");
        doNothing().when(usuarioService).blanquearPassword(10L, "NewPwd123!");

        mockMvc.perform(post("/api/usuarios/10/blanquear-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testEliminarUsuario() throws Exception {
        doNothing().when(usuarioService).eliminarUsuario(10L);

        mockMvc.perform(delete("/api/usuarios/10"))
                .andExpect(status().isNoContent());
    }
}
