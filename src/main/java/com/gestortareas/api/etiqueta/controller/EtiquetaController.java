package com.gestortareas.api.etiqueta.controller;

import com.gestortareas.api.etiqueta.dto.EtiquetaDTO;
import com.gestortareas.api.etiqueta.dto.EtiquetaRequest;
import com.gestortareas.api.etiqueta.service.EtiquetaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/etiquetas")
@RequiredArgsConstructor
public class EtiquetaController {

    private final EtiquetaService etiquetaService;

    @GetMapping("/tablero/{tableroId}")
    @PreAuthorize("@accesoTableroGuard.puedeAccederATablero(authentication, #tableroId)")
    public ResponseEntity<List<EtiquetaDTO>> listarPorTablero(@PathVariable Long tableroId) {
        return ResponseEntity.ok(etiquetaService.listarPorTableroOGlobales(tableroId));
    }

    // Las globales son visibles para cualquier usuario autenticado; solo su gestión es de ADMIN.
    @GetMapping("/globales")
    public ResponseEntity<List<EtiquetaDTO>> listarGlobales() {
        return ResponseEntity.ok(etiquetaService.listarGlobales());
    }

    @PostMapping
    @PreAuthorize("@accesoTableroGuard.puedeCrearEtiqueta(authentication, #request.tableroId)")
    public ResponseEntity<EtiquetaDTO> crearEtiqueta(@Valid @RequestBody EtiquetaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(etiquetaService.crearEtiqueta(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@accesoTableroGuard.puedeAccederAEtiqueta(authentication, #id)")
    public ResponseEntity<EtiquetaDTO> actualizarEtiqueta(@PathVariable Long id,
            @Valid @RequestBody EtiquetaRequest request) {
        return ResponseEntity.ok(etiquetaService.actualizarEtiqueta(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@accesoTableroGuard.puedeAccederAEtiqueta(authentication, #id)")
    public ResponseEntity<Void> eliminarEtiqueta(@PathVariable Long id) {
        etiquetaService.eliminarEtiqueta(id);
        return ResponseEntity.noContent().build();
    }
}
