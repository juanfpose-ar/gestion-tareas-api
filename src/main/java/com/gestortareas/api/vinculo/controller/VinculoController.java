package com.gestortareas.api.vinculo.controller;

import com.gestortareas.api.vinculo.dto.VinculoDTO;
import com.gestortareas.api.vinculo.dto.VinculoRequest;
import com.gestortareas.api.vinculo.service.VinculoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vinculos")
@RequiredArgsConstructor
public class VinculoController {

    private final VinculoService vinculoService;

    @PostMapping
    @PreAuthorize("@accesoTableroGuard.puedeAccederATicket(authentication, #request.ticketOrigenId) "
            + "and @accesoTableroGuard.puedeAccederATicket(authentication, #request.ticketDestinoId)")
    public ResponseEntity<VinculoDTO> crearVinculo(@Valid @RequestBody VinculoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vinculoService.crearVinculo(request));
    }

    @GetMapping("/ticket/{ticketId}")
    @PreAuthorize("@accesoTableroGuard.puedeAccederATicket(authentication, #ticketId)")
    public ResponseEntity<List<VinculoDTO>> obtenerVinculosPorTicket(@PathVariable Long ticketId) {
        return ResponseEntity.ok(vinculoService.obtenerVinculosPorTicket(ticketId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@accesoTableroGuard.puedeAccederAVinculo(authentication, #id)")
    public ResponseEntity<Void> eliminarVinculo(@PathVariable Long id) {
        vinculoService.eliminarVinculo(id);
        return ResponseEntity.noContent().build();
    }
}
