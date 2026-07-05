package com.gestortareas.api.recordatorio.controller;

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

import com.gestortareas.api.recordatorio.dto.RecordatorioDTO;
import com.gestortareas.api.recordatorio.service.RecordatorioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tickets/{ticketId}/recordatorios")
@RequiredArgsConstructor
public class RecordatorioController {

    private final RecordatorioService service;

    @GetMapping
    public ResponseEntity<List<RecordatorioDTO>> getByTicket(@PathVariable Long ticketId) {
        return ResponseEntity.ok(service.findByTicketId(ticketId));
    }

    @PostMapping
    public ResponseEntity<RecordatorioDTO> addRecordatorio(@PathVariable Long ticketId,
            @Valid @RequestBody RecordatorioDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.addRecordatorio(ticketId, request));
    }

    @PutMapping("/{recordatorioId}/notificado")
    public ResponseEntity<RecordatorioDTO> marcarNotificado(@PathVariable Long ticketId,
            @PathVariable Long recordatorioId) {
        return ResponseEntity.ok(service.marcarNotificado(recordatorioId));
    }

    @DeleteMapping("/{recordatorioId}")
    public ResponseEntity<Void> deleteRecordatorio(@PathVariable Long ticketId,
            @PathVariable Long recordatorioId) {
        service.deleteRecordatorio(recordatorioId);
        return ResponseEntity.noContent().build();
    }
}
