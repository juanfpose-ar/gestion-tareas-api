package com.gestortareas.api.usuario.service;

import com.gestortareas.api.usuario.dto.UsuarioDTO;
import com.gestortareas.api.usuario.dto.UsuarioRequest;

import java.util.List;

public interface UsuarioService {
    List<UsuarioDTO> listarTodos();
    UsuarioDTO obtenerPorId(Long id);
    UsuarioDTO obtenerPorUsername(String username);
    UsuarioDTO crearUsuario(UsuarioRequest request);
    UsuarioDTO actualizarUsuario(Long id, UsuarioRequest request);
    void cambiarEstadoActivo(Long id, boolean activo);
    void blanquearPassword(Long id, String passwordNueva);
    void eliminarUsuario(Long id);
}
