package com.gestortareas.api.version.service;

import com.gestortareas.api.version.dto.VersionDTO;
import com.gestortareas.api.version.dto.VersionRequest;

import java.util.List;

public interface VersionService {
    List<VersionDTO> listarPorTablero(Long tableroId);
    VersionDTO crearVersion(VersionRequest request);
    VersionDTO actualizarVersion(Long id, VersionRequest request);
    void eliminarVersion(Long id);
}
