package com.gestortareas.api.usuario.service;

import com.gestortareas.api.enums.Rol;
import com.gestortareas.api.exceptions.BusinessValidationException;
import com.gestortareas.api.exceptions.EntityNotFoundException;
import com.gestortareas.api.tablero.entity.Tablero;
import com.gestortareas.api.tablero.repository.TableroRepository;
import com.gestortareas.api.usuario.dto.UsuarioDTO;
import com.gestortareas.api.usuario.dto.UsuarioRequest;
import com.gestortareas.api.usuario.entity.Usuario;
import com.gestortareas.api.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TableroRepository tableroRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario usuario;
    private Tablero tablero;

    @BeforeEach
    public void setup() {
        tablero = Tablero.builder().id(1L).titulo("Tablero Test").build();
        usuario = Usuario.builder()
                .id(10L)
                .username("user.test")
                .nombreCompleto("User Test")
                .password("encodedPassword")
                .rol(Rol.USER)
                .activo(true)
                .email("test@domain.com")
                .colorAvatar("#ff0000")
                .tablerosAsignados(new HashSet<>(Collections.singletonList(tablero)))
                .build();
    }

    @Test
    public void testListarTodos() {
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

        List<UsuarioDTO> result = usuarioService.listarTodos();

        assertEquals(1, result.size());
        assertEquals("user.test", result.get(0).getUsername());
        assertEquals("User Test", result.get(0).getNombreCompleto());
    }

    @Test
    public void testObtenerPorId_Exitoso() {
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));

        UsuarioDTO result = usuarioService.obtenerPorId(10L);

        assertNotNull(result);
        assertEquals("user.test", result.getUsername());
    }

    @Test
    public void testObtenerPorId_NoEncontrado() {
        when(usuarioRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> usuarioService.obtenerPorId(10L));
    }

    @Test
    public void testObtenerPorUsername_Exitoso() {
        when(usuarioRepository.findByUsername("user.test")).thenReturn(Optional.of(usuario));

        UsuarioDTO result = usuarioService.obtenerPorUsername("user.test");

        assertNotNull(result);
        assertEquals("user.test", result.getUsername());
    }

    @Test
    public void testObtenerPorUsername_NoEncontrado() {
        when(usuarioRepository.findByUsername("user.test")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> usuarioService.obtenerPorUsername("user.test"));
    }

    @Test
    public void testCrearUsuario_Exitoso() {
        UsuarioRequest request = new UsuarioRequest();
        request.setUsername("new.user");
        request.setPassword("pwd");
        request.setNombreCompleto("New User");
        request.setRol(Rol.USER);
        request.setActivo(true);
        request.setEmail("new@mail.com");
        request.setColorAvatar("#ffffff");
        request.setTablerosIds(Set.of(1L));

        when(usuarioRepository.existsByUsername("new.user")).thenReturn(false);
        when(passwordEncoder.encode("pwd")).thenReturn("encodedPwd");
        when(tableroRepository.findAllById(any())).thenReturn(List.of(tablero));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(11L);
            return u;
        });

        UsuarioDTO result = usuarioService.crearUsuario(request);

        assertNotNull(result);
        assertEquals(11L, result.getId());
        assertEquals("new.user", result.getUsername());
        assertTrue(result.getTablerosIds().contains(1L));
    }

    @Test
    public void testCrearUsuario_Duplicado() {
        UsuarioRequest request = new UsuarioRequest();
        request.setUsername("user.test");
        request.setPassword("pwd");
        request.setNombreCompleto("User Test");
        request.setRol(Rol.USER);
        request.setActivo(true);
        request.setEmail("test@domain.com");
        request.setColorAvatar("#ff0000");

        when(usuarioRepository.existsByUsername("user.test")).thenReturn(true);

        assertThrows(BusinessValidationException.class, () -> usuarioService.crearUsuario(request));
    }

    @Test
    public void testCrearUsuario_PasswordVacia() {
        UsuarioRequest request = new UsuarioRequest();
        request.setUsername("new.user");
        request.setPassword("");
        request.setNombreCompleto("New User");
        request.setRol(Rol.USER);
        request.setActivo(true);
        request.setEmail("new@mail.com");
        request.setColorAvatar("#ffffff");

        when(usuarioRepository.existsByUsername("new.user")).thenReturn(false);

        assertThrows(BusinessValidationException.class, () -> usuarioService.crearUsuario(request));
    }

    @Test
    public void testActualizarUsuario_Exitoso() {
        UsuarioRequest request = new UsuarioRequest();
        request.setUsername("user.test");
        request.setPassword("newPwd");
        request.setNombreCompleto("User Test Mod");
        request.setRol(Rol.USER);
        request.setActivo(true);
        request.setEmail("test@domain.com");
        request.setColorAvatar("#00ff00");

        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("newPwd")).thenReturn("encodedNewPwd");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioDTO result = usuarioService.actualizarUsuario(10L, request);

        assertNotNull(result);
        assertEquals("User Test Mod", result.getNombreCompleto());
        assertEquals("#00ff00", result.getColorAvatar());
    }

    @Test
    public void testActualizarUsuario_CambiarUsernameExistente() {
        UsuarioRequest request = new UsuarioRequest();
        request.setUsername("existing.user");
        request.setNombreCompleto("User Test");
        request.setRol(Rol.USER);
        request.setActivo(true);
        request.setEmail("test@domain.com");
        request.setColorAvatar("#ff0000");

        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByUsername("existing.user")).thenReturn(true);

        assertThrows(BusinessValidationException.class, () -> usuarioService.actualizarUsuario(10L, request));
    }

    @Test
    public void testActualizarUsuario_DesactivarAdminFalla() {
        usuario.setRol(Rol.ADMIN);
        UsuarioRequest request = new UsuarioRequest();
        request.setUsername("user.test");
        request.setNombreCompleto("User Test");
        request.setRol(Rol.ADMIN);
        request.setActivo(false);
        request.setEmail("test@domain.com");
        request.setColorAvatar("#ff0000");

        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));

        assertThrows(BusinessValidationException.class, () -> usuarioService.actualizarUsuario(10L, request));
    }

    @Test
    public void testCambiarEstadoActivo_DesactivarAdminFalla() {
        usuario.setRol(Rol.ADMIN);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));

        assertThrows(BusinessValidationException.class, () -> usuarioService.cambiarEstadoActivo(10L, false));
    }

    @Test
    public void testCambiarEstadoActivo_Exitoso() {
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));

        usuarioService.cambiarEstadoActivo(10L, false);

        assertFalse(usuario.isActivo());
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    public void testBlanquearPassword_Exitoso() {
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("newpwd")).thenReturn("encodedNewPwd");

        usuarioService.blanquearPassword(10L, "newpwd");

        assertEquals("encodedNewPwd", usuario.getPassword());
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    public void testEliminarUsuario_Exitoso() {
        when(usuarioRepository.existsById(10L)).thenReturn(true);

        usuarioService.eliminarUsuario(10L);

        verify(usuarioRepository, times(1)).deleteById(10L);
    }

    @Test
    public void testEliminarUsuario_NoEncontrado() {
        when(usuarioRepository.existsById(10L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> usuarioService.eliminarUsuario(10L));
    }
}
