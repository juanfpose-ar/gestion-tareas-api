package com.gestortareas.api.usuario.controller;

import com.gestortareas.api.usuario.dto.BlanqueoPasswordRequest;
import com.gestortareas.api.usuario.dto.UsuarioDTO;
import com.gestortareas.api.usuario.dto.UsuarioRequest;
import com.gestortareas.api.usuario.service.UsuarioService;
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
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    // Abierto a cualquier autenticado porque la mensajería y la asignación de tickets
    // necesitan el directorio; los no-admin reciben el DTO reducido (sin email ni tableros).
    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> listarTodos(@AuthenticationPrincipal UserDetails userDetails) {
        boolean esAdmin = userDetails != null && userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(usuarioService.listarTodos(esAdmin));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioDTO> crearUsuario(@Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crearUsuario(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioDTO> actualizarUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.ok(usuarioService.actualizarUsuario(id, request));
    }

    @PatchMapping("/{id}/activo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cambiarEstadoActivo(@PathVariable Long id, @RequestParam boolean activo) {
        usuarioService.cambiarEstadoActivo(id, activo);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/blanquear-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> blanquearPassword(
            @PathVariable Long id,
            @Valid @RequestBody BlanqueoPasswordRequest request) {
        usuarioService.blanquearPassword(id, request.getPasswordNueva());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
