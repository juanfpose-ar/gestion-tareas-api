package com.gestortareas.api.checklist.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

import com.gestortareas.api.checklist.dto.ChecklistItemDTO;
import com.gestortareas.api.checklist.entity.ChecklistItem;
import com.gestortareas.api.checklist.repository.ChecklistItemRepository;
import com.gestortareas.api.exceptions.EntityNotFoundException;
import com.gestortareas.api.ticket.entity.Ticket;
import com.gestortareas.api.ticket.repository.TicketRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class ChecklistServiceImpl implements ChecklistService {

    private final ChecklistItemRepository itemRepository;
    private final TicketRepository ticketRepository;

    @Override
    public List<ChecklistItemDTO> findByTicketId(Long ticketId) {
        return itemRepository.findByTicketIdOrderByOrden(ticketId)
                .stream().map(ChecklistItem::toDTO).toList();
    }

    @Override
    public ChecklistItemDTO addItem(Long ticketId, ChecklistItemDTO request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket no encontrado: " + ticketId));
        ChecklistItem item = new ChecklistItem();
        item.setTicket(ticket);
        item.setTexto(request.getTexto());
        item.setCompletado(Boolean.TRUE.equals(request.getCompletado()));
        item.setOrden(request.getOrden() != null ? request.getOrden() : (short) 0);
        return itemRepository.save(item).toDTO();
    }

    @Override
    public ChecklistItemDTO updateItem(Long itemId, ChecklistItemDTO request) {
        ChecklistItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item no encontrado: " + itemId));
        item.setTexto(request.getTexto());
        if (request.getCompletado() != null) item.setCompletado(request.getCompletado());
        if (request.getOrden() != null) item.setOrden(request.getOrden());
        return item.toDTO();
    }

    @Override
    public void deleteItem(Long itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new EntityNotFoundException("Item no encontrado: " + itemId);
        }
        itemRepository.deleteById(itemId);
    }

    @Override
    public ChecklistItemDTO toggleItem(Long itemId) {
        ChecklistItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item no encontrado: " + itemId));
        item.setCompletado(!Boolean.TRUE.equals(item.getCompletado()));
        return item.toDTO();
    }

    @Override
    public void reordenar(Long ticketId, List<Long> orderedIds) {
        Map<Long, ChecklistItem> itemMap = itemRepository.findByTicketIdOrderByOrden(ticketId)
                .stream().collect(Collectors.toMap(ChecklistItem::getId, i -> i));
        IntStream.range(0, orderedIds.size()).forEach(i -> {
            ChecklistItem item = itemMap.get(orderedIds.get(i));
            if (item != null) item.setOrden((short) i);
        });
    }
}
