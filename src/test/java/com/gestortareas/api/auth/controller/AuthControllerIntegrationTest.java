package com.gestortareas.api.auth.controller;

import tools.jackson.databind.ObjectMapper;
import com.gestortareas.api.auth.dto.CambiarPasswordRequest;
import com.gestortareas.api.auth.dto.LoginRequest;
import com.gestortareas.api.auth.dto.ProfileUpdateRequest;
import com.gestortareas.api.security.JwtTokenProvider;
import com.gestortareas.api.usuario.dto.UsuarioDTO;
import com.gestortareas.api.usuario.entity.Usuario;
import com.gestortareas.api.usuario.repository.UsuarioRepository;
import com.gestortareas.api.usuario.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtTokenProvider tokenProvider;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    public void testLogin_Exitoso() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("user.test");
        request.setPassword("password");

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user.test");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(tokenProvider.generateToken(auth)).thenReturn("dummyJwtToken");

        Usuario u = Usuario.builder().id(1L).username("user.test").nombreCompleto("User Test").build();
        when(usuarioRepository.findByUsername("user.test")).thenReturn(Optional.of(u));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("dummyJwtToken"))
                .andExpect(jsonPath("$.username").value("user.test"));
    }

    @Test
    public void testLogin_RateLimitTrasDemasiadosFallos() throws Exception {
        // Username propio de este test para no ensuciar el estado del limiter (bean real,
        // compartido por todo el contexto) de cara a otros tests.
        LoginRequest request = new LoginRequest();
        request.setUsername("brute.force.user");
        request.setPassword("wrong-password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("bad"));

        // Los primeros 10 intentos fallidos (default app.login.max-intentos) devuelven 401.
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        // El intento 11 ya no llega a autenticarse: 429.
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    public void testLogout_Exitoso() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user.test")
    public void testGetCurrentUser_Exitoso() throws Exception {
        UsuarioDTO dto = UsuarioDTO.builder().id(1L).username("user.test").build();
        when(usuarioService.obtenerPorUsername("user.test")).thenReturn(dto);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user.test"));
    }

    @Test
    @WithMockUser(username = "user.test")
    public void testCambiarPassword_Exitoso() throws Exception {
        CambiarPasswordRequest request = new CambiarPasswordRequest();
        request.setPasswordActual("OldPwd123!");
        request.setPasswordNueva("NewPwd123!");

        Usuario u = Usuario.builder().id(1L).username("user.test").password("encodedOld").build();
        when(usuarioRepository.findByUsername("user.test")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("OldPwd123!", "encodedOld")).thenReturn(true);
        when(passwordEncoder.matches("NewPwd123!", "encodedOld")).thenReturn(false);
        when(passwordEncoder.encode("NewPwd123!")).thenReturn("encodedNew");

        mockMvc.perform(post("/api/auth/cambiar-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user.test")
    public void testUpdateProfile_Exitoso() throws Exception {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setUsername("user.test");
        request.setNombreCompleto("User Updated");

        Usuario u = Usuario.builder().id(1L).username("user.test").build();
        when(usuarioRepository.findByUsername("user.test")).thenReturn(Optional.of(u));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(u);

        UsuarioDTO dto = UsuarioDTO.builder().id(1L).username("user.test").build();
        when(usuarioService.obtenerPorId(1L)).thenReturn(dto);

        mockMvc.perform(put("/api/auth/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user.test"));
    }
}
