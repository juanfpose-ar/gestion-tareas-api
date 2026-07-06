package com.gestortareas.api.tablero.controller;

import com.gestortareas.api.tablero.dto.TableroDTO;
import com.gestortareas.api.tablero.dto.TableroRequest;
import com.gestortareas.api.tablero.service.TableroService;
import com.gestortareas.api.usuario.dto.UsuarioDTO;
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
@RequestMapping("/api/tableros")
@RequiredArgsConstructor
public class TableroController {

    private final TableroService tableroService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    public ResponseEntity<List<TableroDTO>> listarVisibles(@AuthenticationPrincipal UserDetails userDetails) {
        boolean esAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(tableroService.listarVisiblesParaUsuario(userDetails.getUsername(), esAdmin));
    }

    @GetMapping("/admin/todos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TableroDTO>> listarTodos() {
        return ResponseEntity.ok(tableroService.listarTodosParaAdmin());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@accesoTableroGuard.puedeAccederATablero(authentication, #id)")
    public ResponseEntity<TableroDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(tableroService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TableroDTO> crearTablero(@Valid @RequestBody TableroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tableroService.crearTablero(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TableroDTO> actualizarTablero(
            @PathVariable Long id,
            @Valid @RequestBody TableroRequest request) {
        return ResponseEntity.ok(tableroService.actualizarTablero(id, request));
    }

    @PatchMapping("/{id}/archivar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TableroDTO> archivarTablero(
            @PathVariable Long id,
            @RequestParam boolean archivado) {
        return ResponseEntity.ok(tableroService.archivarTablero(id, archivado));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarTablero(@PathVariable Long id) {
        tableroService.eliminarTablero(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/miembros")
    @PreAuthorize("@accesoTableroGuard.puedeAccederATablero(authentication, #id)")
    public ResponseEntity<List<UsuarioDTO>> getMiembros(@PathVariable Long id) {
        List<Usuario> miembros = usuarioRepository.findByTablerosAsignadosId(id);
        List<UsuarioDTO> dtos = miembros.stream()
                .map(u -> UsuarioDTO.builder()
                        .id(u.getId()).username(u.getUsername()).nombreCompleto(u.getNombreCompleto())
                        .rol(u.getRol()).activo(u.isActivo()).build())
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
