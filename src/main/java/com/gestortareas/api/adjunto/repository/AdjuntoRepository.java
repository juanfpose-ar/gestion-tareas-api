package com.gestortareas.api.adjunto.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestortareas.api.adjunto.entity.Adjunto;

public interface AdjuntoRepository extends JpaRepository<Adjunto, Long> {

    List<Adjunto> findByTicketIdOrderByFechaSubidaDesc(Long ticketId);
    int countByTicketId(Long ticketId);
    Optional<Adjunto> findByIdAndTicketId(Long id, Long ticketId);
}
