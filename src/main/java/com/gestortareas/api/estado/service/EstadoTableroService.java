package com.gestortareas.api.estado.service;

import com.gestortareas.api.estado.dto.EstadoTableroDTO;
import com.gestortareas.api.estado.dto.EstadoTableroRequest;

import java.util.List;

public interface EstadoTableroService {
    List<EstadoTableroDTO> listarPorTablero(Long tableroId);
    EstadoTableroDTO crearEstado(EstadoTableroRequest request);
    EstadoTableroDTO actualizarEstado(Long id, EstadoTableroRequest request);
    void eliminarEstado(Long id);
}
