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
import com.gestortareas.api.security.MembresiaTableroCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final TableroRepository tableroRepository;
    private final PasswordEncoder passwordEncoder;
    private final MembresiaTableroCacheService membresiaTableroCacheService;

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioDTO> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioDTO obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));
        return mapToDTO(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioDTO obtenerPorUsername(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con username: " + username));
        return mapToDTO(usuario);
    }

    @Override
    @Transactional
    public UsuarioDTO crearUsuario(UsuarioRequest request) {
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new BusinessValidationException("El usuario ya existe");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BusinessValidationException("La contraseña es obligatoria para crear un usuario");
        }

        Set<Tablero> tableros = obtenerTablerosPorIds(request.getTablerosIds());

        Usuario usuario = Usuario.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .nombreCompleto(request.getNombreCompleto())
                .rol(request.getRol())
                .activo(request.isActivo())
                .email(request.getEmail())
                .colorAvatar(request.getColorAvatar() != null ? request.getColorAvatar() : "#0d6efd")
                .tablerosAsignados(tableros)
                .build();

        Usuario saved = usuarioRepository.save(usuario);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public UsuarioDTO actualizarUsuario(Long id, UsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));

        if (!usuario.getUsername().equals(request.getUsername()) && usuarioRepository.existsByUsername(request.getUsername())) {
            throw new BusinessValidationException("El nombre de usuario ya está en uso");
        }

        if (usuario.getRol() == Rol.ADMIN && !request.isActivo()) {
            throw new BusinessValidationException("No se puede desactivar un usuario con rol ADMIN");
        }

        usuario.setUsername(request.getUsername());
        usuario.setNombreCompleto(request.getNombreCompleto());
        usuario.setRol(request.getRol());
        usuario.setActivo(request.isActivo());
        usuario.setEmail(request.getEmail());
        if (request.getColorAvatar() != null) {
            usuario.setColorAvatar(request.getColorAvatar());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        boolean tablerosActualizados = request.getTablerosIds() != null;
        if (tablerosActualizados) {
            usuario.setTablerosAsignados(obtenerTablerosPorIds(request.getTablerosIds()));
        }

        Usuario updated = usuarioRepository.save(usuario);

        if (tablerosActualizados) {
            membresiaTableroCacheService.limpiarCache();
        }

        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void cambiarEstadoActivo(Long id, boolean activo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));
        if (usuario.getRol() == Rol.ADMIN && !activo) {
            throw new BusinessValidationException("No se puede desactivar un usuario con rol ADMIN");
        }
        usuario.setActivo(activo);
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void blanquearPassword(Long id, String passwordNueva) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));
        usuario.setPassword(passwordEncoder.encode(passwordNueva));
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void eliminarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new EntityNotFoundException("Usuario no encontrado con ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    private Set<Tablero> obtenerTablerosPorIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(tableroRepository.findAllById(ids));
    }

    private UsuarioDTO mapToDTO(Usuario u) {
        Set<Long> tablerosIds = u.getTablerosAsignados() == null ? new HashSet<>()
                : u.getTablerosAsignados().stream().map(Tablero::getId).collect(Collectors.toSet());

        return UsuarioDTO.builder()
                .id(u.getId())
                .username(u.getUsername())
                .nombreCompleto(u.getNombreCompleto())
                .rol(u.getRol())
                .activo(u.isActivo())
                .tablerosIds(tablerosIds)
                .email(u.getEmail())
                .colorAvatar(u.getColorAvatar() != null ? u.getColorAvatar() : "#0d6efd")
                .build();
    }
}
