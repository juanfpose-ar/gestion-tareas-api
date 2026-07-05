package com.gestortareas.api.recordatorio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestortareas.api.enums.TipoRecordatorio;
import com.gestortareas.api.recordatorio.entity.Recordatorio;

public interface RecordatorioRepository extends JpaRepository<Recordatorio, Long> {

    Optional<Recordatorio> findByTicketIdAndTipo(Long ticketId, TipoRecordatorio tipo);

    List<Recordatorio> findByTicketId(Long ticketId);
    int countByTicketId(Long ticketId);
}
