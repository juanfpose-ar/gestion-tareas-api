package com.gestortareas.api.mensajeria.service;

import com.gestortareas.api.mensajeria.dto.*;

import java.util.List;

public interface MensajeriaService {
    List<ConversacionResumenDTO> obtenerBandeja(String username);
    ConversacionDetalleDTO obtenerConversacion(Long id, String username);
    ConversacionResumenDTO crearConversacion(NuevaConversacionRequest request, String username);
    ConversacionDetalleDTO responderConversacion(Long id, ResponderConversacionRequest request, String username);
    void actualizarEstado(Long id, ActualizarEstadoRequest request, String username);
}
