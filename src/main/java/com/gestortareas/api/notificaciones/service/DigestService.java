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
import lombok.extern.log4j.Log4j2;
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
@Log4j2
public class DigestService {

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
        log.info("Cron digestMediodia disparado");
        enviarDigestATodos();
    }

    @Scheduled(cron = "${app.digest.cron.noche}")
    public void digestNoche() {
        log.info("Cron digestNoche disparado");
        enviarDigestATodos();
    }

    @Transactional
    public void enviarDigestATodos() {
        LocalDateTime ahora = LocalDateTime.now();
        List<Usuario> usuarios = usuarioRepository.findByActivoTrue();
        log.info("Iniciando envío de digest: {} usuario(s) activo(s)", usuarios.size());

        // Reuniones candidatas: rango amplio, se filtra el recordatorio exacto por usuario más abajo
        List<Reunion> reunionesCercanas = reunionRepository.findByFechaBetween(
                ahora.toLocalDate().minusDays(1), ahora.toLocalDate().plusDays(2));

        int enviados = 0;
        int sinEmail = 0;
        int errores = 0;

        for (Usuario usuario : usuarios) {
            if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
                sinEmail++;
                continue;
            }
            try {
                if (procesarUsuario(usuario, ahora, reunionesCercanas)) {
                    enviados++;
                }
            } catch (Exception e) {
                errores++;
                log.error("Error generando digest para {}: {}", usuario.getUsername(), e.getMessage());
            }
        }

        log.info("Digest finalizado: {} enviado(s), {} sin novedades, {} sin email, {} con error (de {} activos)",
                enviados, usuarios.size() - enviados - sinEmail - errores, sinEmail, errores, usuarios.size());
    }

    private boolean procesarUsuario(Usuario usuario, LocalDateTime ahora, List<Reunion> reunionesCercanas) {
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
            log.debug("Sin novedades para {}, no se envía digest", usuario.getUsername());
            return false;
        }

        String cuerpo = construirCuerpo(usuario, ticketsActualizados, ticketsVencidos,
                recordatoriosVencidos, reunionesConRecordatorio, mensajesSinLeer);

        mailService.enviar(usuario.getEmail(), "GestorTareas — Tenés novedades", cuerpo);
        log.info("Digest enviado a {} ({} ticket(s) actualizado(s), {} vencido(s), {} recordatorio(s), "
                        + "{} reunión(es), {} mensaje(s) sin leer)",
                usuario.getUsername(), ticketsActualizados.size(), ticketsVencidos.size(),
                recordatoriosVencidos.size(), reunionesConRecordatorio.size(), mensajesSinLeer);

        usuario.setUltimoDigestEnviado(ahora);
        usuarioRepository.save(usuario);
        return true;
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
