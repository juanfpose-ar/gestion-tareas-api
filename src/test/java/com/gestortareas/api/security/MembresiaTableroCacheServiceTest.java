package com.gestortareas.api.security;

import com.gestortareas.api.enums.Rol;
import com.gestortareas.api.exceptions.EntityNotFoundException;
import com.gestortareas.api.tablero.entity.Tablero;
import com.gestortareas.api.usuario.entity.Usuario;
import com.gestortareas.api.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Prueba el contrato de caching real (proxy de Spring + Caffeine), no solo la lógica:
 * un unit test puro no detectaría una anotación @Cacheable mal puesta.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MembresiaTableroCacheServiceTest.TestConfig.class)
public class MembresiaTableroCacheServiceTest {

    @Configuration
    @EnableCaching
    static class TestConfig {
        @Bean
        UsuarioRepository usuarioRepository() {
            return Mockito.mock(UsuarioRepository.class);
        }

        @Bean
        MembresiaTableroCacheService membresiaTableroCacheService(UsuarioRepository usuarioRepository) {
            return new MembresiaTableroCacheService(usuarioRepository);
        }

        @Bean
        CacheManager cacheManager() {
            return new CaffeineCacheManager(MembresiaTableroCacheService.CACHE_NAME);
        }
    }

    @Autowired
    private MembresiaTableroCacheService service;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario usuario;

    @BeforeEach
    public void setup() {
        // El contexto (y el mock) se comparten entre tests: limpiar estado siempre.
        Mockito.reset(usuarioRepository);
        service.limpiarCache();

        Tablero tablero = Tablero.builder().id(1L).titulo("Tablero Test").build();
        usuario = Usuario.builder()
                .id(10L).username("user").rol(Rol.USER).activo(true)
                .tablerosAsignados(Set.of(tablero))
                .build();
    }

    @Test
    public void testEsMiembro_TrueYFalseSegunAsignacion() {
        when(usuarioRepository.findByUsername("user")).thenReturn(Optional.of(usuario));

        assertTrue(service.esMiembro("user", 1L));
        assertFalse(service.esMiembro("user", 2L));
    }

    @Test
    public void testSegundaConsultaSaleDeCacheSinTocarLaBase() {
        when(usuarioRepository.findByUsername("user")).thenReturn(Optional.of(usuario));

        service.esMiembro("user", 1L);
        service.esMiembro("user", 1L);
        service.esMiembro("user", 1L);

        verify(usuarioRepository, times(1)).findByUsername("user");
    }

    @Test
    public void testClavesDistintasSonEntradasDistintas() {
        when(usuarioRepository.findByUsername("user")).thenReturn(Optional.of(usuario));

        service.esMiembro("user", 1L);
        service.esMiembro("user", 2L);

        verify(usuarioRepository, times(2)).findByUsername("user");
    }

    @Test
    public void testLimpiarCacheInvalidaTodo() {
        when(usuarioRepository.findByUsername("user")).thenReturn(Optional.of(usuario));

        service.esMiembro("user", 1L);
        service.limpiarCache();
        service.esMiembro("user", 1L);

        verify(usuarioRepository, times(2)).findByUsername("user");
    }

    @Test
    public void testUsuarioNoEncontradoLanzaYNoSeCachea() {
        when(usuarioRepository.findByUsername("fantasma")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.esMiembro("fantasma", 1L));
        assertThrows(EntityNotFoundException.class, () -> service.esMiembro("fantasma", 1L));

        // @Cacheable no cachea excepciones: cada intento vuelve a consultar.
        verify(usuarioRepository, times(2)).findByUsername("fantasma");
    }
}
