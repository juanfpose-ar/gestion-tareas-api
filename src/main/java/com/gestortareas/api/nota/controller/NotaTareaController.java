package com.gestortareas.api.nota.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gestortareas.api.nota.dto.NotaTareaDTO;
import com.gestortareas.api.nota.service.NotaTareaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tickets/{ticketId}/notas")
@RequiredArgsConstructor
public class NotaTareaController {

    private final NotaTareaService service;

    @GetMapping
    public ResponseEntity<List<NotaTareaDTO>> getByTicket(@PathVariable Long ticketId) {
        return ResponseEntity.ok(service.findByTicketId(ticketId));
    }

    @PostMapping
    public ResponseEntity<NotaTareaDTO> addNota(@PathVariable Long ticketId,
            @Valid @RequestBody NotaTareaDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addNota(ticketId, request));
    }

    @PutMapping("/{notaId}")
    public ResponseEntity<NotaTareaDTO> updateNota(@PathVariable Long ticketId,
            @PathVariable Long notaId, @Valid @RequestBody NotaTareaDTO request) {
        return ResponseEntity.ok(service.updateNota(notaId, request));
    }

    @DeleteMapping("/{notaId}")
    public ResponseEntity<Void> deleteNota(@PathVariable Long ticketId,
            @PathVariable Long notaId) {
        service.deleteNota(notaId);
        return ResponseEntity.noContent().build();
    }
}
