package com.gestortareas.api.adjunto.service;

import com.gestortareas.api.exceptions.BusinessValidationException;
import com.gestortareas.api.exceptions.EntityNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

/**
 * Guarda los archivos adjuntos en disco (bajo un volumen de Docker en producción).
 * Cada archivo se guarda con un nombre único bajo una subcarpeta por ticket.
 */
@Service
@Log4j2
public class FileStorageService {

    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "svg",
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "txt", "csv", "zip", "rar"
    );

    private final Path root;

    public FileStorageService(@Value("${app.uploads.dir}") String uploadsDir) {
        this.root = Paths.get(uploadsDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo crear el directorio de uploads: " + root, e);
        }
    }

    public String guardar(Long ticketId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessValidationException("El archivo está vacío");
        }

        String extension = obtenerExtension(file.getOriginalFilename());
        if (!EXTENSIONES_PERMITIDAS.contains(extension.toLowerCase())) {
            throw new BusinessValidationException("Tipo de archivo no permitido: ." + extension);
        }

        String nombreArchivo = UUID.randomUUID() + "_" + sanitizar(file.getOriginalFilename());
        String rutaRelativa = ticketId + "/" + nombreArchivo;
        Path destino = resolverDentroDeRoot(rutaRelativa);

        try {
            Files.createDirectories(destino.getParent());
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error guardando el archivo en disco", e);
        }

        return rutaRelativa;
    }

    public Resource cargar(String rutaRelativa) {
        Path archivo = resolverDentroDeRoot(rutaRelativa);
        if (!Files.exists(archivo)) {
            throw new EntityNotFoundException("Archivo no encontrado en disco");
        }
        return new FileSystemResource(archivo);
    }

    public void eliminar(String rutaRelativa) {
        if (rutaRelativa == null || rutaRelativa.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(resolverDentroDeRoot(rutaRelativa));
        } catch (IOException e) {
            log.warn("No se pudo borrar el archivo {}: {}", rutaRelativa, e.getMessage());
        }
    }

    /** Resuelve la ruta relativa dentro de root, evitando path traversal (../). */
    private Path resolverDentroDeRoot(String rutaRelativa) {
        Path resuelto = root.resolve(rutaRelativa).normalize();
        if (!resuelto.startsWith(root)) {
            throw new BusinessValidationException("Ruta de archivo inválida");
        }
        return resuelto;
    }

    private String obtenerExtension(String filename) {
        if (filename == null) return "";
        int idx = filename.lastIndexOf('.');
        return idx >= 0 && idx < filename.length() - 1 ? filename.substring(idx + 1) : "";
    }

    private String sanitizar(String filename) {
        if (filename == null || filename.isBlank()) return "archivo";
        String base = Paths.get(filename).getFileName().toString();
        return base.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
