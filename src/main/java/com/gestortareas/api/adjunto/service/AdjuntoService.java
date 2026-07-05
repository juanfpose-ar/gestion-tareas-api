package com.gestortareas.api.adjunto.service;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.gestortareas.api.adjunto.dto.AdjuntoDTO;
import com.gestortareas.api.adjunto.dto.EnlaceRequest;

public interface AdjuntoService {

    List<AdjuntoDTO> findByTicketId(Long ticketId);

    AdjuntoDTO uploadArchivo(Long ticketId, MultipartFile file);

    AdjuntoDTO addEnlace(Long ticketId, EnlaceRequest request);

    void deleteAdjunto(Long ticketId, Long adjuntoId);

    record ArchivoDescargable(Resource resource, String nombre, String contentType) {}

    ArchivoDescargable descargarArchivo(Long ticketId, Long adjuntoId);
}
