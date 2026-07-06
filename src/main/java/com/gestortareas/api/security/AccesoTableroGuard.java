package com.gestortareas.api.security;

import com.gestortareas.api.estado.repository.EstadoTableroRepository;
import com.gestortareas.api.etiqueta.entity.Etiqueta;
import com.gestortareas.api.etiqueta.repository.EtiquetaRepository;
import com.gestortareas.api.exceptions.EntityNotFoundException;
import com.gestortareas.api.reunion.repository.ReunionRepository;
import com.gestortareas.api.ticket.repository.TicketRepository;
import com.gestortareas.api.version.repository.VersionRepository;
import com.gestortareas.api.vinculo.repository.TicketVinculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Punto único de control de acceso: toda acción sobre un ticket/tablero exige que quien la
 * ejecuta pertenezca a ese tablero (o sea ADMIN, que ve todo).
 *
 * Se usa exclusivamente desde anotaciones {@code @PreAuthorize("@accesoTableroGuard.puedeAccederA...(authentication, #param)")}
 * en los controllers — así el chequeo es declarativo (una línea en la firma del método) en vez
 * de repetir la misma llamada imperativa en el cuerpo de cada endpoint.
 */
@Component
@RequiredArgsConstructor
public class AccesoTableroGuard {

    private final TicketRepository ticketRepository;
    private final VersionRepository versionRepository;
    private final EstadoTableroRepository estadoTableroRepository;
    private final TicketVinculoRepository ticketVinculoRepository;
    private final ReunionRepository reunionRepository;
    private final EtiquetaRepository etiquetaRepository;
    private final MembresiaTableroCacheService membresiaTableroCacheService;

    public boolean puedeAccederATablero(Authentication authentication, Long tableroId) {
        if (esAdmin(authentication)) {
            return true;
        }
        return membresiaTableroCacheService.esMiembro(authentication.getName(), tableroId);
    }

    public boolean puedeAccederATicket(Authentication authentication, Long ticketId) {
        if (esAdmin(authentication)) {
            return true;
        }
        Long tableroId = ticketRepository.findTableroIdByTicketId(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket no encontrado: " + ticketId));
        return puedeAccederATablero(authentication, tableroId);
    }

    public boolean puedeAccederAVersion(Authentication authentication, Long versionId) {
        if (esAdmin(authentication)) {
            return true;
        }
        Long tableroId = versionRepository.findTableroIdByVersionId(versionId)
                .orElseThrow(() -> new EntityNotFoundException("Versión no encontrada: " + versionId));
        return puedeAccederATablero(authentication, tableroId);
    }

    public boolean puedeAccederAEstado(Authentication authentication, Long estadoId) {
        if (esAdmin(authentication)) {
            return true;
        }
        Long tableroId = estadoTableroRepository.findTableroIdByEstadoId(estadoId)
                .orElseThrow(() -> new EntityNotFoundException("Estado no encontrado: " + estadoId));
        return puedeAccederATablero(authentication, tableroId);
    }

    public boolean puedeAccederAVinculo(Authentication authentication, Long vinculoId) {
        if (esAdmin(authentication)) {
            return true;
        }
        Long ticketOrigenId = ticketVinculoRepository.findTicketOrigenIdById(vinculoId)
                .orElseThrow(() -> new EntityNotFoundException("Vínculo no encontrado: " + vinculoId));
        return puedeAccederATicket(authentication, ticketOrigenId);
    }

    public boolean puedeAccederAReunion(Authentication authentication, Long reunionId) {
        if (esAdmin(authentication)) {
            return true;
        }
        Long tableroId = reunionRepository.findTableroIdByReunionId(reunionId)
                .orElseThrow(() -> new EntityNotFoundException("Reunión no encontrada: " + reunionId));
        return puedeAccederATablero(authentication, tableroId);
    }

    /**
     * Etiquetas: las que tienen tablero siguen la regla de membresía; las globales
     * (tablero null) solo las gestiona un ADMIN.
     */
    public boolean puedeAccederAEtiqueta(Authentication authentication, Long etiquetaId) {
        if (esAdmin(authentication)) {
            return true;
        }
        Etiqueta etiqueta = etiquetaRepository.findById(etiquetaId)
                .orElseThrow(() -> new EntityNotFoundException("Etiqueta no encontrada: " + etiquetaId));
        if (etiqueta.getTablero() == null) {
            return false;
        }
        return puedeAccederATablero(authentication, etiqueta.getTablero().getId());
    }

    /**
     * Para crear etiquetas: con tableroId aplica membresía; sin tableroId (global) solo ADMIN.
     */
    public boolean puedeCrearEtiqueta(Authentication authentication, Long tableroId) {
        if (esAdmin(authentication)) {
            return true;
        }
        if (tableroId == null) {
            return false;
        }
        return puedeAccederATablero(authentication, tableroId);
    }

    private boolean esAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
