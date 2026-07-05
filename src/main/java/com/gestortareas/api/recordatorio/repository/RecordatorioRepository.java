package com.gestortareas.api.recordatorio.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestortareas.api.enums.TipoRecordatorio;
import com.gestortareas.api.recordatorio.entity.Recordatorio;

public interface RecordatorioRepository extends JpaRepository<Recordatorio, Long> {

    Optional<Recordatorio> findByTicketIdAndTipo(Long ticketId, TipoRecordatorio tipo);

    List<Recordatorio> findByTicketId(Long ticketId);
    int countByTicketId(Long ticketId);

    @Query("SELECT DISTINCT r FROM Recordatorio r JOIN r.ticket t LEFT JOIN t.asignados a LEFT JOIN t.informados i " +
           "WHERE (a.id = :usuarioId OR i.id = :usuarioId) " +
           "AND r.fechaPersonalizada IS NOT NULL " +
           "AND r.fechaPersonalizada > :desde AND r.fechaPersonalizada <= :ahora")
    List<Recordatorio> findVencidosParaUsuario(
            @Param("usuarioId") Long usuarioId,
            @Param("desde") LocalDateTime desde,
            @Param("ahora") LocalDateTime ahora
    );
}
