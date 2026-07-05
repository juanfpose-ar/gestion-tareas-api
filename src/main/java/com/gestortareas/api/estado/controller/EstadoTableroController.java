package com.gestortareas.api.estado.controller;

import com.gestortareas.api.estado.dto.EstadoTableroDTO;
import com.gestortareas.api.estado.dto.EstadoTableroRequest;
import com.gestortareas.api.estado.service.EstadoTableroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estados")
@RequiredArgsConstructor
public class EstadoTableroController {

    private final EstadoTableroService estadoService;

    @GetMapping("/tablero/{tableroId}")
    public ResponseEntity<List<EstadoTableroDTO>> listarPorTablero(@PathVariable Long tableroId) {
        return ResponseEntity.ok(estadoService.listarPorTablero(tableroId));
    }

    @PostMapping
    public ResponseEntity<EstadoTableroDTO> crearEstado(@Valid @RequestBody EstadoTableroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(estadoService.crearEstado(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EstadoTableroDTO> actualizarEstado(@PathVariable Long id,
            @Valid @RequestBody EstadoTableroRequest request) {
        return ResponseEntity.ok(estadoService.actualizarEstado(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarEstado(@PathVariable Long id) {
        estadoService.eliminarEstado(id);
        return ResponseEntity.noContent().build();
    }
}
