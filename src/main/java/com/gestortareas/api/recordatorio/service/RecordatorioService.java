package com.gestortareas.api.recordatorio.service;

import java.util.List;

import com.gestortareas.api.recordatorio.dto.RecordatorioDTO;

public interface RecordatorioService {

    List<RecordatorioDTO> findByTicketId(Long ticketId);

    RecordatorioDTO addRecordatorio(Long ticketId, RecordatorioDTO request);

    RecordatorioDTO marcarNotificado(Long recordatorioId);

    void deleteRecordatorio(Long recordatorioId);
}
