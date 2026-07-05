package com.gestortareas.api.adjunto.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gestortareas.api.adjunto.dto.AdjuntoDTO;
import com.gestortareas.api.adjunto.dto.EnlaceRequest;
import com.gestortareas.api.adjunto.service.AdjuntoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tickets/{ticketId}/adjuntos")
@RequiredArgsConstructor
public class AdjuntoController {

    private final AdjuntoService service;

    @GetMapping
    public ResponseEntity<List<AdjuntoDTO>> getByTicket(@PathVariable Long ticketId) {
        return ResponseEntity.ok(service.findByTicketId(ticketId));
    }

    @PostMapping(value = "/archivo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AdjuntoDTO> uploadArchivo(@PathVariable Long ticketId,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.uploadArchivo(ticketId, file));
    }

    @PostMapping("/enlace")
    public ResponseEntity<AdjuntoDTO> addEnlace(@PathVariable Long ticketId,
            @Valid @RequestBody EnlaceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.addEnlace(ticketId, request));
    }

    @DeleteMapping("/{adjuntoId}")
    public ResponseEntity<Void> deleteAdjunto(@PathVariable Long ticketId,
            @PathVariable Long adjuntoId) {
        service.deleteAdjunto(adjuntoId);
        return ResponseEntity.noContent().build();
    }
}
