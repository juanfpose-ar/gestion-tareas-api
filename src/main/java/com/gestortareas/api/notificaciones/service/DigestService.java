package com.gestortareas.api.notificaciones.service;

import com.gestortareas.api.mensajeria.repository.UsuarioConversacionRepository;
import com.gestortareas.api.recordatorio.entity.Recordatorio;
import com.gestortareas.api.recordatorio.repository.RecordatorioRepository;
import com.gestortareas.api.reunion.entity.Reunion;
import com.gestortareas.api.reunion.repository.ReunionRepository;
import com.gestortareas.api.ticket.entity.Ticket;
import com.gestortareas.api.ticket.repository.TicketRepository;
import com.gestortareas.api.usuario.entity.Usuario;
import com.gestortareas.api.usuario.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Digest de novedades (tickets, recordatorios, reuniones, bandeja de entrada) enviado por mail
 * a las 00:00 y 19:00. Solo se envía si el usuario tiene algo nuevo desde el último envío.
 */
@Service
public class DigestService {

    private static final Logger log = LoggerFactory.getLogger(DigestService.class);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private RecordatorioRepository recordatorioRepository;

    @Autowired
    private ReunionRepository reunionRepository;

    @Autowired
    private UsuarioConversacionRepository usuarioConversacionRepository;

    @Autowired
    private MailService mailService;

    @Scheduled(cron = "${app.digest.cron.mediodia}")
    public void digestMediodia() {
        enviarDigestATodos();
    }

    @Scheduled(cron = "${app.digest.cron.noche}")
    public void digestNoche() {
        enviarDigestATodos();
    }

    @Transactional
    public void enviarDigestATodos() {
        LocalDateTime ahora = LocalDateTime.now();
        List<Usuario> usuarios = usuarioRepository.findByActivoTrue();

        // Reuniones candidatas: rango amplio, se filtra el recordatorio exacto por usuario más abajo
        List<Reunion> reunionesCercanas = reunionRepository.findByFechaBetween(
                ahora.toLocalDate().minusDays(1), ahora.toLocalDate().plusDays(2));

        for (Usuario usuario : usuarios) {
            if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
                continue;
            }
            try {
                procesarUsuario(usuario, ahora, reunionesCercanas);
            } catch (Exception e) {
                log.error("Error generando digest para {}: {}", usuario.getUsername(), e.getMessage());
            }
        }
    }

    private void procesarUsuario(Usuario usuario, LocalDateTime ahora, List<Reunion> reunionesCercanas) {
        LocalDateTime desde = usuario.getUltimoDigestEnviado() != null
                ? usuario.getUltimoDigestEnviado()
                : ahora.minusHours(24);

        List<Ticket> ticketsRelevantes = ticketRepository.findRelevantesParaUsuario(usuario.getId());

        List<Ticket> ticketsActualizados = ticketsRelevantes.stream()
                .filter(t -> t.getFechaModificacion() != null && t.getFechaModificacion().isAfter(desde))
                .toList();

        List<Ticket> ticketsVencidos = ticketsRelevantes.stream()
                .filter(t -> t.getFechaVencimiento() != null
                        && !t.getFechaVencimiento().isAfter(ahora)
                        && t.getFechaVencimiento().isAfter(desde))
                .toList();

        List<Recordatorio> recordatoriosVencidos = recordatorioRepository.findVencidosParaUsuario(usuario.getId(), desde, ahora);

        List<Reunion> reunionesConRecordatorio = reunionesCercanas.stream()
                .filter(r -> usuario.getTablerosAsignados().contains(r.getTablero()))
                .filter(r -> {
                    LocalDateTime recordatorio = calcularRecordatorioReunion(r);
                    return recordatorio != null && recordatorio.isAfter(desde) && !recordatorio.isAfter(ahora);
                })
                .toList();

        int mensajesSinLeer = usuarioConversacionRepository
                .countByUsuarioIdAndLeidaFalseAndArchivadaFalseAndEliminadaFalse(usuario.getId());

        boolean hayNovedades = !ticketsActualizados.isEmpty()
                || !ticketsVencidos.isEmpty()
                || !recordatoriosVencidos.isEmpty()
                || !reunionesConRecordatorio.isEmpty()
                || mensajesSinLeer > 0;

        if (!hayNovedades) {
            return;
        }

        String cuerpo = construirCuerpo(usuario, ticketsActualizados, ticketsVencidos,
                recordatoriosVencidos, reunionesConRecordatorio, mensajesSinLeer);

        mailService.enviar(usuario.getEmail(), "GestorTareas — Tenés novedades", cuerpo);

        usuario.setUltimoDigestEnviado(ahora);
        usuarioRepository.save(usuario);
    }

    private LocalDateTime calcularRecordatorioReunion(Reunion r) {
        if (r.getHoraInicio() == null || r.getRecordatorioMinutos() == null) {
            return null;
        }
        try {
            LocalTime hora = LocalTime.parse(r.getHoraInicio());
            return LocalDateTime.of(r.getFecha(), hora).minusMinutes(r.getRecordatorioMinutos());
        } catch (Exception e) {
            return null;
        }
    }

    private String construirCuerpo(Usuario usuario, List<Ticket> actualizados, List<Ticket> vencidos,
                                     List<Recordatorio> recordatorios, List<Reunion> reuniones, int mensajesSinLeer) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hola ").append(usuario.getNombreCompleto()).append(",\n\n");
        sb.append("Tenés novedades en GestorTareas:\n\n");

        if (!actualizados.isEmpty()) {
            sb.append("TICKETS ACTUALIZADOS:\n");
            for (Ticket t : actualizados) {
                sb.append(" - #").append(t.getId()).append(" \"").append(t.getTitulo())
                  .append("\" (actualizado ").append(t.getFechaModificacion().format(FMT)).append(")\n");
            }
            sb.append("\n");
        }

        if (!vencidos.isEmpty()) {
            sb.append("TICKETS VENCIDOS:\n");
            for (Ticket t : vencidos) {
                sb.append(" - #").append(t.getId()).append(" \"").append(t.getTitulo())
                  .append("\" (venció ").append(t.getFechaVencimiento().format(FMT)).append(")\n");
            }
            sb.append("\n");
        }

        if (!recordatorios.isEmpty()) {
            sb.append("RECORDATORIOS DE TICKETS:\n");
            for (Recordatorio r : recordatorios) {
                sb.append(" - #").append(r.getTicket().getId()).append(" \"").append(r.getTicket().getTitulo())
                  .append("\": recordatorio venció ").append(r.getFechaPersonalizada().format(FMT)).append("\n");
            }
            sb.append("\n");
        }

        if (!reuniones.isEmpty()) {
            sb.append("RECORDATORIOS DE REUNIONES:\n");
            for (Reunion r : reuniones) {
                sb.append(" - \"").append(r.getTitulo()).append("\" el ")
                  .append(r.getFecha()).append(" a las ").append(r.getHoraInicio()).append("\n");
            }
            sb.append("\n");
        }

        if (mensajesSinLeer > 0) {
            sb.append("BANDEJA DE ENTRADA:\n");
            sb.append(" - Tenés ").append(mensajesSinLeer).append(" conversación(es) sin leer.\n\n");
        }

        sb.append("Saludos,\nGestorTareas\n");
        return sb.toString();
    }
}
