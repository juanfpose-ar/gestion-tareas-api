package com.gestortareas.api.adjunto.service;

import com.gestortareas.api.exceptions.BusinessValidationException;
import com.gestortareas.api.exceptions.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileStorageService service;

    @BeforeEach
    public void setup() {
        service = new FileStorageService(tempDir.toString());
    }

    private MockMultipartFile archivo(String nombre, String contenido) {
        return new MockMultipartFile("file", nombre, "text/plain", contenido.getBytes());
    }

    // ---- guardar ----

    @Test
    public void testGuardar_EscribeEnSubcarpetaDelTicket() throws Exception {
        String ruta = service.guardar(1L, archivo("notas.txt", "contenido de prueba"));

        assertTrue(ruta.startsWith("1/"), "la ruta relativa arranca con el id del ticket: " + ruta);
        Path enDisco = tempDir.resolve(ruta);
        assertTrue(Files.exists(enDisco));
        assertEquals("contenido de prueba", Files.readString(enDisco));
    }

    @Test
    public void testGuardar_SanitizaElNombre() {
        String ruta = service.guardar(1L, archivo("mi archivo (raro) ñ.txt", "x"));

        // Después del UUID_, solo quedan caracteres seguros.
        String nombreFinal = ruta.substring(ruta.indexOf('_') + 1);
        assertTrue(nombreFinal.matches("[a-zA-Z0-9._-]+"), "nombre sanitizado: " + nombreFinal);
        assertTrue(nombreFinal.endsWith(".txt"));
    }

    @Test
    public void testGuardar_ArchivoVacio() {
        MockMultipartFile vacio = new MockMultipartFile("file", "vacio.txt", "text/plain", new byte[0]);

        assertThrows(BusinessValidationException.class, () -> service.guardar(1L, vacio));
    }

    @Test
    public void testGuardar_ExtensionNoPermitida() {
        assertThrows(BusinessValidationException.class,
                () -> service.guardar(1L, archivo("malware.exe", "MZ")));
    }

    @Test
    public void testGuardar_SinExtension() {
        assertThrows(BusinessValidationException.class,
                () -> service.guardar(1L, archivo("archivo-sin-extension", "x")));
    }

    // ---- cargar ----

    @Test
    public void testCargar_DevuelveElArchivoGuardado() throws Exception {
        String ruta = service.guardar(2L, archivo("doc.pdf", "%PDF-fake"));

        Resource resource = service.cargar(ruta);

        assertTrue(resource.exists());
        assertEquals("%PDF-fake", new String(resource.getInputStream().readAllBytes()));
    }

    @Test
    public void testCargar_NoExisteEnDisco() {
        assertThrows(EntityNotFoundException.class, () -> service.cargar("1/no-existe.txt"));
    }

    @Test
    public void testCargar_PathTraversalBloqueado() {
        assertThrows(BusinessValidationException.class,
                () -> service.cargar("../../etc/passwd"));
    }

    // ---- eliminar ----

    @Test
    public void testEliminar_BorraElArchivo() {
        String ruta = service.guardar(3L, archivo("borrar.txt", "x"));
        assertTrue(Files.exists(tempDir.resolve(ruta)));

        service.eliminar(ruta);

        assertFalse(Files.exists(tempDir.resolve(ruta)));
    }

    @Test
    public void testEliminar_InexistenteONuloNoLanza() {
        assertDoesNotThrow(() -> service.eliminar("9/no-existe.txt"));
        assertDoesNotThrow(() -> service.eliminar(null));
        assertDoesNotThrow(() -> service.eliminar(" "));
    }
}
