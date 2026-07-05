package com.gestortareas.api.security;

import com.gestortareas.api.enums.Rol;
import com.gestortareas.api.usuario.entity.Usuario;
import com.gestortareas.api.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    public void testLoadUserByUsername_Exitoso() {
        Usuario u = Usuario.builder().username("user").password("pwd").rol(Rol.USER).activo(true).build();
        when(usuarioRepository.findByUsername("user")).thenReturn(Optional.of(u));

        UserDetails result = userDetailsService.loadUserByUsername("user");

        assertNotNull(result);
        assertEquals("user", result.getUsername());
        assertEquals("pwd", result.getPassword());
        assertTrue(result.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    public void testLoadUserByUsername_Inactivo() {
        Usuario u = Usuario.builder().username("user").password("pwd").rol(Rol.USER).activo(false).build();
        when(usuarioRepository.findByUsername("user")).thenReturn(Optional.of(u));

        assertThrows(DisabledException.class, () -> userDetailsService.loadUserByUsername("user"));
    }

    @Test
    public void testLoadUserByUsername_NoEncontrado() {
        when(usuarioRepository.findByUsername("user")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("user"));
    }
}
