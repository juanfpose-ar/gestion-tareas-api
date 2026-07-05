package com.gestortareas.api.checklist.service;

import java.util.List;

import com.gestortareas.api.checklist.dto.ChecklistItemDTO;

public interface ChecklistService {

    List<ChecklistItemDTO> findByTicketId(Long ticketId);

    ChecklistItemDTO addItem(Long ticketId, ChecklistItemDTO request);

    ChecklistItemDTO updateItem(Long itemId, ChecklistItemDTO request);

    void deleteItem(Long itemId);

    ChecklistItemDTO toggleItem(Long itemId);

    void reordenar(Long ticketId, List<Long> orderedIds);
}
