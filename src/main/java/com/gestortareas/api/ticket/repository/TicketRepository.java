package com.gestortareas.api.ticket.repository;

import com.gestortareas.api.ticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByTableroIdAndArchivadoFalseOrderByOrdenAscIdAsc(Long tableroId);
    List<Ticket> findByTableroIdAndArchivadoFalse(Long tableroId);
    List<Ticket> findByTableroIdAndArchivadoTrueOrderByFechaArchivadoDesc(Long tableroId);
    List<Ticket> findByArchivadoTrueOrderByFechaArchivadoDesc();
    List<Ticket> findByEstadoIdAndArchivadoFalse(Long estadoId);
    int countByEstadoIdAndArchivadoFalse(Long estadoId);
    List<Ticket> findByVersionId(Long versionId);
}
