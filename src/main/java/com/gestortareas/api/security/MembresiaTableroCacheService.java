package com.gestortareas.api.security;

import com.gestortareas.api.exceptions.EntityNotFoundException;
import com.gestortareas.api.usuario.entity.Usuario;
import com.gestortareas.api.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Cachea si un usuario pertenece a un tablero — es la consulta que dispara
 * AccesoTableroGuard en cada acción sobre un ticket/tablero, así que evita pegarle
 * a la base en cada request. Se invalida por completo cuando un admin edita los
 * tableros asignados de un usuario (ver UsuarioServiceImpl.actualizarUsuario).
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class MembresiaTableroCacheService {

    public static final String CACHE_NAME = "membresiaTablero";

    private final UsuarioRepository usuarioRepository;

    @Cacheable(value = CACHE_NAME, key = "#username + ':' + #tableroId")
    public boolean esMiembro(String username, Long tableroId) {
        // Solo se llega acá en un cache miss: un hit nunca ejecuta el cuerpo del método.
        log.info("Cache miss [{}]: consultando en base si '{}' pertenece al tablero {}", CACHE_NAME, username, tableroId);
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + username));
        boolean esMiembro = usuario.getTablerosAsignados().stream().anyMatch(t -> t.getId().equals(tableroId));
        log.debug("Resultado membresía: '{}' {} al tablero {}", username, esMiembro ? "pertenece" : "no pertenece", tableroId);
        return esMiembro;
    }

    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void limpiarCache() {
        log.info("Cache [{}] invalidada por completo (cambio de membresía a tablero)", CACHE_NAME);
    }
}
