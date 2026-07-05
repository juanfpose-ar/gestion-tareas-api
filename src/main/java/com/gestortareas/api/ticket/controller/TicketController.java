package com.gestortareas.api.ticket.controller;

import com.gestortareas.api.ticket.dto.TicketCardDTO;
import com.gestortareas.api.ticket.dto.TicketDTO;
import com.gestortareas.api.ticket.dto.TicketRequest;
import com.gestortareas.api.ticket.service.TicketService;
import com.gestortareas.api.usuario.entity.Usuario;
import com.gestortareas.api.usuario.repository.UsuarioRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping("/tablero/{tableroId}")
    @PreAuthorize("@accesoTableroGuard.puedeAccederATablero(authentication, #tableroId)")
    public ResponseEntity<List<TicketCardDTO>> listarActivosPorTablero(@PathVariable Long tableroId) {
        return ResponseEntity.ok(ticketService.listarActivosPorTablero(tableroId));
    }

    @GetMapping("/tablero/{tableroId}/archivados")
    @PreAuthorize("@accesoTableroGuard.puedeAccederATablero(authentication, #tableroId)")
    public ResponseEntity<List<TicketCardDTO>> listarArchivadosPorTablero(@PathVariable Long tableroId) {
        return ResponseEntity.ok(ticketService.listarArchivadosPorTablero(tableroId));
    }

    @GetMapping("/version/{versionId}")
    @PreAuthorize("@accesoTableroGuard.puedeAccederAVersion(authentication, #versionId)")
    public ResponseEntity<List<TicketCardDTO>> listarPorVersion(@PathVariable Long versionId) {
        return ResponseEntity.ok(ticketService.listarPorVersion(versionId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@accesoTableroGuard.puedeAccederATicket(authentication, #id)")
    public ResponseEntity<TicketDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("@accesoTableroGuard.puedeAccederATablero(authentication, #request.tableroId)")
    public ResponseEntity<TicketDTO> crearTicket(
            @Valid @RequestBody TicketRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long creadorId = null;
        if (userDetails != null) {
            Usuario u = usuarioRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (u != null) creadorId = u.getId();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.crearTicket(request, creadorId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@accesoTableroGuard.puedeAccederATicket(authentication, #id)")
    public ResponseEntity<TicketDTO> actualizarTicket(
            @PathVariable Long id,
            @Valid @RequestBody TicketRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long modificadorId = null;
        if (userDetails != null) {
            Usuario u = usuarioRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (u != null) modificadorId = u.getId();
        }
        return ResponseEntity.ok(ticketService.actualizarTicket(id, request, modificadorId));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("@accesoTableroGuard.puedeAccederATicket(authentication, #id)")
    public ResponseEntity<TicketDTO> cambiarEstado(@PathVariable Long id, @RequestParam Long nuevoEstadoId) {
        return ResponseEntity.ok(ticketService.cambiarEstado(id, nuevoEstadoId));
    }

    @PutMapping("/estado/{estadoId}/reordenar")
    @PreAuthorize("@accesoTableroGuard.puedeAccederAEstado(authentication, #estadoId)")
    public ResponseEntity<Void> reordenarEnEstado(@PathVariable Long estadoId, @RequestBody List<Long> orderedIds) {
        ticketService.reordenarEnEstado(estadoId, orderedIds);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/archivar")
    @PreAuthorize("@accesoTableroGuard.puedeAccederATicket(authentication, #id)")
    public ResponseEntity<TicketDTO> cambiarArchivado(@PathVariable Long id, @RequestParam boolean archivado) {
        return ResponseEntity.ok(ticketService.cambiarArchivado(id, archivado));
    }

    @PatchMapping("/{id}/completado")
    @PreAuthorize("@accesoTableroGuard.puedeAccederATicket(authentication, #id)")
    public ResponseEntity<TicketDTO> cambiarCompletado(@PathVariable Long id, @RequestParam boolean completado) {
        return ResponseEntity.ok(ticketService.cambiarCompletado(id, completado));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@accesoTableroGuard.puedeAccederATicket(authentication, #id)")
    public ResponseEntity<Void> eliminarTicket(@PathVariable Long id) {
        ticketService.eliminarTicket(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/asignados/{usuarioId}")
    @PreAuthorize("@accesoTableroGuard.puedeAccederATicket(authentication, #id)")
    public ResponseEntity<TicketDTO> asignarUsuario(@PathVariable Long id, @PathVariable Long usuarioId) {
        return ResponseEntity.ok(ticketService.asignarUsuario(id, usuarioId));
    }

    @DeleteMapping("/{id}/asignados/{usuarioId}")
    @PreAuthorize("@accesoTableroGuard.puedeAccederATicket(authentication, #id)")
    public ResponseEntity<TicketDTO> desasignarUsuario(@PathVariable Long id, @PathVariable Long usuarioId) {
        return ResponseEntity.ok(ticketService.desasignarUsuario(id, usuarioId));
    }

    @PostMapping("/{id}/informados/{usuarioId}")
    @PreAuthorize("@accesoTableroGuard.puedeAccederATicket(authentication, #id)")
    public ResponseEntity<TicketDTO> asignarInformado(@PathVariable Long id, @PathVariable Long usuarioId) {
        return ResponseEntity.ok(ticketService.asignarInformado(id, usuarioId));
    }

    @DeleteMapping("/{id}/informados/{usuarioId}")
    @PreAuthorize("@accesoTableroGuard.puedeAccederATicket(authentication, #id)")
    public ResponseEntity<TicketDTO> desasignarInformado(@PathVariable Long id, @PathVariable Long usuarioId) {
        return ResponseEntity.ok(ticketService.desasignarInformado(id, usuarioId));
    }
}
