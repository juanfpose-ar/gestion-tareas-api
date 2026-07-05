package com.gestortareas.api.checklist.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gestortareas.api.checklist.dto.ChecklistItemDTO;
import com.gestortareas.api.checklist.service.ChecklistService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tickets/{ticketId}/checklist")
@RequiredArgsConstructor
@PreAuthorize("@accesoTableroGuard.puedeAccederATicket(authentication, #ticketId)")
public class ChecklistController {

    private final ChecklistService service;

    @GetMapping
    public ResponseEntity<List<ChecklistItemDTO>> getByTicket(@PathVariable Long ticketId) {
        return ResponseEntity.ok(service.findByTicketId(ticketId));
    }

    @PostMapping
    public ResponseEntity<ChecklistItemDTO> addItem(@PathVariable Long ticketId,
            @Valid @RequestBody ChecklistItemDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addItem(ticketId, request));
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<ChecklistItemDTO> updateItem(@PathVariable Long ticketId,
            @PathVariable Long itemId, @Valid @RequestBody ChecklistItemDTO request) {
        return ResponseEntity.ok(service.updateItem(itemId, request));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long ticketId,
            @PathVariable Long itemId) {
        service.deleteItem(itemId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{itemId}/toggle")
    public ResponseEntity<ChecklistItemDTO> toggleItem(@PathVariable Long ticketId,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(service.toggleItem(itemId));
    }

    @PutMapping("/reordenar")
    public ResponseEntity<Void> reordenar(@PathVariable Long ticketId,
            @RequestBody List<Long> orderedIds) {
        service.reordenar(ticketId, orderedIds);
        return ResponseEntity.noContent().build();
    }
}
