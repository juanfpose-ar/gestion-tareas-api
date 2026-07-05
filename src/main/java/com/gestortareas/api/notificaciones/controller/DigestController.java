package com.gestortareas.api.notificaciones.controller;

import com.gestortareas.api.notificaciones.service.DigestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/digest")
public class DigestController {

    @Autowired
    private DigestService digestService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/enviar")
    public ResponseEntity<Void> enviarAhora() {
        digestService.enviarDigestATodos();
        return ResponseEntity.noContent().build();
    }
}
