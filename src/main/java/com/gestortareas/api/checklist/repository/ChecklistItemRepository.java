package com.gestortareas.api.checklist.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestortareas.api.checklist.entity.ChecklistItem;

public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Long> {

    List<ChecklistItem> findByTicketIdOrderByOrden(Long ticketId);
    int countByTicketId(Long ticketId);
    int countByTicketIdAndCompletadoTrue(Long ticketId);
}
