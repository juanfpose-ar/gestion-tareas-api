package com.gestortareas.api.reunion.service;

import com.gestortareas.api.reunion.dto.ReunionDTO;
import com.gestortareas.api.reunion.dto.ReunionRequest;

import java.util.List;

public interface ReunionService {
    List<ReunionDTO> findByTableroId(Long tableroId);
    ReunionDTO crear(ReunionRequest request);
    ReunionDTO actualizar(Long id, ReunionRequest request);
    void eliminar(Long id);
}
