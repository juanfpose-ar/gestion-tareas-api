package com.gestortareas.api.mensajeria.service;

import com.gestortareas.api.exceptions.BusinessValidationException;
import com.gestortareas.api.mensajeria.dto.*;
import com.gestortareas.api.mensajeria.entity.*;
import com.gestortareas.api.mensajeria.repository.*;
import com.gestortareas.api.usuario.entity.Usuario;
import com.gestortareas.api.usuario.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MensajeriaServiceImpl implements MensajeriaService {

    @Autowired
    private ConversacionRepository conversacionRepository;

    @Autowired
    private MensajeRepository mensajeRepository;

    @Autowired
    private UsuarioConversacionRepository usuarioConversacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ConversacionResumenDTO> obtenerBandeja(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + username));

        List<UsuarioConversacion> ucs = usuarioConversacionRepository.findActiveConversationsForUser(usuario.getId());

        return ucs.stream().map(uc -> {
            Conversacion c = uc.getConversacion();
            Mensaje ultimo = mensajeRepository.findFirstByConversacionIdOrderByFechaEnvioDesc(c.getId());
            int total = c.getMensajes().size();
            
            Long ultimoEmisorId = ultimo != null ? ultimo.getEmisor().getId() : null;
            String ultimoEmisorNombre = ultimo != null ? ultimo.getEmisor().getNombreCompleto() : "";
            String fragmento = (ultimo != null && ultimo.getContenido() != null) ? ultimo.getContenido() : "";

            String nombresParticipantes = c.getParticipantes().stream()
                    .map(p -> p.getUsuario())
                    .filter(u -> !u.getUsername().equals(username))
                    .map(u -> u.getNombreCompleto() != null && !u.getNombreCompleto().isEmpty() ? u.getNombreCompleto() : u.getUsername())
                    .collect(Collectors.joining(", "));
            if (nombresParticipantes.isEmpty()) {
                nombresParticipantes = "Yo";
            }

            return new ConversacionResumenDTO(
                c.getId(),
                c.getAsunto(),
                ultimoEmisorId,
                ultimoEmisorNombre,
                fragmento,
                total,
                !uc.isLeida(),
                c.getFechaUltimaActividad(),
                uc.isArchivada(),
                nombresParticipantes
            );
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ConversacionDetalleDTO obtenerConversacion(Long id, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + username));

        Conversacion c = conversacionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Conversación no encontrada: " + id));

        UsuarioConversacion uc = usuarioConversacionRepository.findByUsuarioIdAndConversacionId(usuario.getId(), c.getId())
                .orElseThrow(() -> new BusinessValidationException("No tienes acceso a esta conversación"));

        if (uc.isEliminada()) {
            throw new BusinessValidationException("No tienes acceso a esta conversación");
        }

        // Marcar automáticamente como leída
        uc.setLeida(true);
        usuarioConversacionRepository.save(uc);

        List<MensajeDTO> msgs = mensajeRepository.findByConversacionIdOrderByFechaEnvioAsc(c.getId()).stream()
                .map(m -> new MensajeDTO(
                    m.getId(),
                    m.getEmisor().getId(),
                    m.getEmisor().getNombreCompleto(),
                    m.getContenido(),
                    m.getFechaEnvio()
                )).collect(Collectors.toList());

        List<Long> participanteIds = c.getParticipantes().stream()
                .map(p -> p.getUsuario().getId())
                .collect(Collectors.toList());

        return new ConversacionDetalleDTO(
            c.getId(),
            c.getAsunto(),
            c.getFechaUltimaActividad(),
            msgs,
            participanteIds
        );
    }

    @Override
    @Transactional
    public ConversacionResumenDTO crearConversacion(NuevaConversacionRequest request, String username) {
        Usuario emisor = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + username));

        Conversacion conversacion = Conversacion.builder()
                .asunto(request.asunto())
                .fechaUltimaActividad(LocalDateTime.now())
                .build();

        Conversacion savedConversacion = conversacionRepository.save(conversacion);

        Mensaje mensaje = Mensaje.builder()
                .conversacion(savedConversacion)
                .emisor(emisor)
                .contenido(request.contenido())
                .fechaEnvio(LocalDateTime.now())
                .build();

        mensajeRepository.save(mensaje);

        // Participantes únicos
        Set<Long> destinatarioIds = new HashSet<>(request.destinatarioIds());
        destinatarioIds.add(emisor.getId());

        List<UsuarioConversacion> ucs = new ArrayList<>();
        for (Long uId : destinatarioIds) {
            Usuario u = usuarioRepository.findById(uId)
                    .orElseThrow(() -> new EntityNotFoundException("Destinatario no encontrado: " + uId));

            boolean esEmisor = uId.equals(emisor.getId());
            UsuarioConversacion uc = UsuarioConversacion.builder()
                    .usuario(u)
                    .conversacion(savedConversacion)
                    .leida(esEmisor)
                    .archivada(false)
                    .eliminada(false)
                    .build();

            ucs.add(usuarioConversacionRepository.save(uc));
        }

        savedConversacion.setParticipantes(ucs);
        conversacionRepository.save(savedConversacion);

        String nombresParticipantes = savedConversacion.getParticipantes().stream()
                .map(p -> p.getUsuario())
                .filter(u -> !u.getUsername().equals(username))
                .map(u -> u.getNombreCompleto() != null && !u.getNombreCompleto().isEmpty() ? u.getNombreCompleto() : u.getUsername())
                .collect(Collectors.joining(", "));
        if (nombresParticipantes.isEmpty()) {
            nombresParticipantes = "Yo";
        }

        return new ConversacionResumenDTO(
                savedConversacion.getId(),
                savedConversacion.getAsunto(),
                emisor.getId(),
                emisor.getNombreCompleto(),
                request.contenido(),
                1,
                false,
                savedConversacion.getFechaUltimaActividad(),
                false,
                nombresParticipantes
        );
    }

    @Override
    @Transactional
    public ConversacionDetalleDTO responderConversacion(Long id, ResponderConversacionRequest request, String username) {
        Usuario emisor = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + username));

        Conversacion conversacion = conversacionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Conversación no encontrada: " + id));

        UsuarioConversacion ucEmisor = usuarioConversacionRepository.findByUsuarioIdAndConversacionId(emisor.getId(), conversacion.getId())
                .orElseThrow(() -> new BusinessValidationException("No participas en esta conversación"));

        if (ucEmisor.isEliminada()) {
            throw new BusinessValidationException("No se puede responder a una conversación eliminada");
        }

        Mensaje mensaje = Mensaje.builder()
                .conversacion(conversacion)
                .emisor(emisor)
                .contenido(request.contenido())
                .fechaEnvio(LocalDateTime.now())
                .build();

        mensajeRepository.save(mensaje);

        conversacion.setFechaUltimaActividad(LocalDateTime.now());
        conversacionRepository.save(conversacion);

        // Actualizar estados para todos los participantes
        List<UsuarioConversacion> todosUc = conversacion.getParticipantes();
        for (UsuarioConversacion uc : todosUc) {
            if (uc.getUsuario().getId().equals(emisor.getId())) {
                uc.setLeida(true);
                uc.setArchivada(false);
                uc.setEliminada(false);
            } else {
                uc.setLeida(false); // Tiene no leídos
                uc.setArchivada(false); // Desarchivar para que aparezca en bandeja
                uc.setEliminada(false); // Deseliminar para que le aparezca el hilo activo
            }
            usuarioConversacionRepository.save(uc);
        }

        List<MensajeDTO> msgs = mensajeRepository.findByConversacionIdOrderByFechaEnvioAsc(conversacion.getId()).stream()
                .map(m -> new MensajeDTO(
                        m.getId(),
                        m.getEmisor().getId(),
                        m.getEmisor().getNombreCompleto(),
                        m.getContenido(),
                        m.getFechaEnvio()
                )).collect(Collectors.toList());

        List<Long> participanteIds = todosUc.stream()
                .map(p -> p.getUsuario().getId())
                .collect(Collectors.toList());

        return new ConversacionDetalleDTO(
                conversacion.getId(),
                conversacion.getAsunto(),
                conversacion.getFechaUltimaActividad(),
                msgs,
                participanteIds
        );
    }

    @Override
    @Transactional
    public void actualizarEstado(Long id, ActualizarEstadoRequest request, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + username));

        UsuarioConversacion uc = usuarioConversacionRepository.findByUsuarioIdAndConversacionId(usuario.getId(), id)
                .orElseThrow(() -> new BusinessValidationException("No participas en esta conversación"));

        if (request.archivada() != null) {
            uc.setArchivada(request.archivada());
        }
        if (request.eliminada() != null) {
            uc.setEliminada(request.eliminada());
        }
        usuarioConversacionRepository.save(uc);
    }
}
