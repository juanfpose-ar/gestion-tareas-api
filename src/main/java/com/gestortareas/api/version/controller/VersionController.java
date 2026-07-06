package com.gestortareas.api.version.controller;

import com.gestortareas.api.version.dto.VersionDTO;
import com.gestortareas.api.version.dto.VersionRequest;
import com.gestortareas.api.version.service.VersionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class VersionController {

    @Autowired
    private VersionService versionService;

    @GetMapping("/tableros/{tableroId}/versiones")
    @PreAuthorize("@accesoTableroGuard.puedeAccederATablero(authentication, #tableroId)")
    public ResponseEntity<List<VersionDTO>> listarPorTablero(@PathVariable Long tableroId) {
        return ResponseEntity.ok(versionService.listarPorTablero(tableroId));
    }

    @PostMapping("/versiones")
    @PreAuthorize("@accesoTableroGuard.puedeAccederATablero(authentication, #request.tableroId)")
    public ResponseEntity<VersionDTO> crearVersion(@Valid @RequestBody VersionRequest request) {
        return ResponseEntity.ok(versionService.crearVersion(request));
    }

    @PutMapping("/versiones/{id}")
    @PreAuthorize("@accesoTableroGuard.puedeAccederAVersion(authentication, #id)")
    public ResponseEntity<VersionDTO> actualizarVersion(@PathVariable Long id,
            @Valid @RequestBody VersionRequest request) {
        return ResponseEntity.ok(versionService.actualizarVersion(id, request));
    }

    @DeleteMapping("/versiones/{id}")
    @PreAuthorize("@accesoTableroGuard.puedeAccederAVersion(authentication, #id)")
    public ResponseEntity<Void> eliminarVersion(@PathVariable Long id) {
        versionService.eliminarVersion(id);
        return ResponseEntity.noContent().build();
    }
}
