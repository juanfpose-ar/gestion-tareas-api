package com.gestortareas.api.etiqueta.service;

import com.gestortareas.api.etiqueta.dto.EtiquetaDTO;
import com.gestortareas.api.etiqueta.dto.EtiquetaRequest;

import java.util.List;

public interface EtiquetaService {
    List<EtiquetaDTO> listarPorTableroOGlobales(Long tableroId);
    List<EtiquetaDTO> listarGlobales();
    EtiquetaDTO crearEtiqueta(EtiquetaRequest request);
    EtiquetaDTO actualizarEtiqueta(Long id, EtiquetaRequest request);
    void eliminarEtiqueta(Long id);
}
