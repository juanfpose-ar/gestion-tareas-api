package com.gestortareas.api.nota.service;

import java.util.List;

import com.gestortareas.api.nota.dto.NotaTareaDTO;

public interface NotaTareaService {

    List<NotaTareaDTO> findByTicketId(Long ticketId);

    NotaTareaDTO addNota(Long ticketId, NotaTareaDTO request);

    NotaTareaDTO updateNota(Long notaId, NotaTareaDTO request);

    void deleteNota(Long notaId);
}
