package com.gestortareas.api.security;

import com.gestortareas.api.exceptions.EntityNotFoundException;
import com.gestortareas.api.usuario.entity.Usuario;
import com.gestortareas.api.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
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
public class MembresiaTableroCacheService {

    public static final String CACHE_NAME = "membresiaTablero";

    private final UsuarioRepository usuarioRepository;

    @Cacheable(value = CACHE_NAME, key = "#username + ':' + #tableroId")
    public boolean esMiembro(String username, Long tableroId) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + username));
        return usuario.getTablerosAsignados().stream().anyMatch(t -> t.getId().equals(tableroId));
    }

    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void limpiarCache() {
        // El borrado lo hace la anotación @CacheEvict; no hay nada más que hacer acá.
    }
}
