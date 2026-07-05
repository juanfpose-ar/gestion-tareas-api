package com.gestortareas.api.vinculo.service;

import com.gestortareas.api.vinculo.dto.VinculoDTO;
import com.gestortareas.api.vinculo.dto.VinculoRequest;

import java.util.List;

public interface VinculoService {
    VinculoDTO crearVinculo(VinculoRequest request);
    List<VinculoDTO> obtenerVinculosPorTicket(Long ticketId);
    void eliminarVinculo(Long id);
}
