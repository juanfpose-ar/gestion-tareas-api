package com.gestortareas.api.ticket.repository;

import com.gestortareas.api.ticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByTableroIdAndArchivadoFalseOrderByOrdenAscIdAsc(Long tableroId);
    List<Ticket> findByTableroIdAndArchivadoFalse(Long tableroId);
    List<Ticket> findByTableroIdAndArchivadoTrueOrderByFechaArchivadoDesc(Long tableroId);
    List<Ticket> findByArchivadoTrueOrderByFechaArchivadoDesc();
    List<Ticket> findByEstadoIdAndArchivadoFalse(Long estadoId);
    int countByEstadoIdAndArchivadoFalse(Long estadoId);
    List<Ticket> findByVersionId(Long versionId);

    @Query("SELECT DISTINCT t FROM Ticket t LEFT JOIN t.asignados a LEFT JOIN t.informados i " +
           "WHERE t.archivado = false AND (a.id = :usuarioId OR i.id = :usuarioId)")
    List<Ticket> findRelevantesParaUsuario(@Param("usuarioId") Long usuarioId);

    @Query("SELECT t.tablero.id FROM Ticket t WHERE t.id = :ticketId")
    Optional<Long> findTableroIdByTicketId(@Param("ticketId") Long ticketId);
}
