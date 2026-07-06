package com.gestortareas.api.reunion.controller;

import com.gestortareas.api.reunion.dto.ReunionDTO;
import com.gestortareas.api.reunion.dto.ReunionRequest;
import com.gestortareas.api.reunion.service.ReunionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReunionController {

    private final ReunionService reunionService;

    @GetMapping("/tableros/{tableroId}/reuniones")
    @PreAuthorize("@accesoTableroGuard.puedeAccederATablero(authentication, #tableroId)")
    public ResponseEntity<List<ReunionDTO>> listarPorTablero(@PathVariable Long tableroId) {
        return ResponseEntity.ok(reunionService.findByTableroId(tableroId));
    }

    @PostMapping("/reuniones")
    @PreAuthorize("@accesoTableroGuard.puedeAccederATablero(authentication, #request.tableroId)")
    public ResponseEntity<ReunionDTO> crear(@Valid @RequestBody ReunionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reunionService.crear(request));
    }

    @PutMapping("/reuniones/{id}")
    @PreAuthorize("@accesoTableroGuard.puedeAccederAReunion(authentication, #id)")
    public ResponseEntity<ReunionDTO> actualizar(@PathVariable Long id,
            @Valid @RequestBody ReunionRequest request) {
        return ResponseEntity.ok(reunionService.actualizar(id, request));
    }

    @DeleteMapping("/reuniones/{id}")
    @PreAuthorize("@accesoTableroGuard.puedeAccederAReunion(authentication, #id)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        reunionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
