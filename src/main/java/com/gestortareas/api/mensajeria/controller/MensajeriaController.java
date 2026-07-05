package com.gestortareas.api.mensajeria.controller;

import com.gestortareas.api.mensajeria.dto.*;
import com.gestortareas.api.mensajeria.service.MensajeriaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mensajeria")
public class MensajeriaController {

    @Autowired
    private MensajeriaService mensajeriaService;

    @GetMapping("/bandeja")
    public ResponseEntity<List<ConversacionResumenDTO>> obtenerBandeja(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        return ResponseEntity.ok(mensajeriaService.obtenerBandeja(username));
    }

    @GetMapping("/conversaciones/{id}")
    public ResponseEntity<ConversacionDetalleDTO> obtenerConversacion(
            @PathVariable Long id, 
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        return ResponseEntity.ok(mensajeriaService.obtenerConversacion(id, username));
    }

    @PostMapping("/conversaciones")
    public ResponseEntity<ConversacionResumenDTO> crearConversacion(
            @Valid @RequestBody NuevaConversacionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mensajeriaService.crearConversacion(request, username));
    }

    @PostMapping("/conversaciones/{id}/responder")
    public ResponseEntity<ConversacionDetalleDTO> responderConversacion(
            @PathVariable Long id,
            @Valid @RequestBody ResponderConversacionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mensajeriaService.responderConversacion(id, request, username));
    }

    @PatchMapping("/conversaciones/{id}/estado")
    public ResponseEntity<Void> actualizarEstado(
            @PathVariable Long id,
            @RequestBody ActualizarEstadoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        mensajeriaService.actualizarEstado(id, request, username);
        return ResponseEntity.noContent().build();
    }
}
