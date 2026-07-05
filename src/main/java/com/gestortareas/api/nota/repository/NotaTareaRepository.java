package com.gestortareas.api.nota.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestortareas.api.nota.entity.NotaTarea;

public interface NotaTareaRepository extends JpaRepository<NotaTarea, Long> {

    List<NotaTarea> findByTicketIdOrderByFechaHoraDesc(Long ticketId);
    int countByTicketId(Long ticketId);
}
