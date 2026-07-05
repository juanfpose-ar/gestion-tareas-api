package com.gestortareas.api.tablero.service;

import com.gestortareas.api.estado.entity.EstadoTablero;
import com.gestortareas.api.estado.repository.EstadoTableroRepository;
import com.gestortareas.api.exceptions.EntityNotFoundException;
import com.gestortareas.api.tablero.dto.TableroDTO;
import com.gestortareas.api.tablero.dto.TableroRequest;
import com.gestortareas.api.tablero.entity.Tablero;
import com.gestortareas.api.tablero.repository.TableroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TableroServiceImplTest {

    @Mock
    private TableroRepository tableroRepository;

    @Mock
    private EstadoTableroRepository estadoTableroRepository;

    @InjectMocks
    private TableroServiceImpl tableroService;

    private Tablero tablero;

    @BeforeEach
    public void setup() {
        tablero = Tablero.builder()
                .id(10L)
                .titulo("Work Project")
                .descripcion("Kanban for work")
                .imagenFondoUrl("http://image.url")
                .fechaCreacion(LocalDateTime.now())
                .archivado(false)
                .build();
    }

    @Test
    public void testListarVisiblesParaUsuario_Admin() {
        when(tableroRepository.findAll()).thenReturn(List.of(tablero));

        List<TableroDTO> result = tableroService.listarVisiblesParaUsuario("admin", true);

        assertEquals(1, result.size());
        assertEquals("Work Project", result.get(0).getTitulo());
    }

    @Test
    public void testListarVisiblesParaUsuario_User() {
        when(tableroRepository.findTablerosAsignadosAUsuario("user")).thenReturn(List.of(tablero));

        List<TableroDTO> result = tableroService.listarVisiblesParaUsuario("user", false);

        assertEquals(1, result.size());
        assertEquals("Work Project", result.get(0).getTitulo());
    }

    @Test
    public void testListarTodosParaAdmin() {
        when(tableroRepository.findAll()).thenReturn(List.of(tablero));

        List<TableroDTO> result = tableroService.listarTodosParaAdmin();

        assertEquals(1, result.size());
    }

    @Test
    public void testObtenerPorId_Exitoso() {
        when(tableroRepository.findById(10L)).thenReturn(Optional.of(tablero));

        TableroDTO result = tableroService.obtenerPorId(10L);

        assertNotNull(result);
        assertEquals("Work Project", result.getTitulo());
    }

    @Test
    public void testObtenerPorId_NoEncontrado() {
        when(tableroRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> tableroService.obtenerPorId(10L));
    }

    @Test
    public void testCrearTablero_Exitoso() {
        TableroRequest request = new TableroRequest();
        request.setTitulo("New Board");
        request.setDescripcion("Description");
        request.setImagenFondoUrl("http://bg.url");

        when(tableroRepository.save(any(Tablero.class))).thenAnswer(inv -> {
            Tablero t = inv.getArgument(0);
            t.setId(11L);
            return t;
        });

        TableroDTO result = tableroService.crearTablero(request);

        assertNotNull(result);
        assertEquals(11L, result.getId());
        assertEquals("New Board", result.getTitulo());
        verify(estadoTableroRepository, times(3)).save(any(EstadoTablero.class));
    }

    @Test
    public void testActualizarTablero_Exitoso() {
        TableroRequest request = new TableroRequest();
        request.setTitulo("Updated Board");
        request.setDescripcion("New desc");
        request.setImagenFondoUrl("http://new.bg.url");

        when(tableroRepository.findById(10L)).thenReturn(Optional.of(tablero));
        when(tableroRepository.save(any(Tablero.class))).thenAnswer(inv -> inv.getArgument(0));

        TableroDTO result = tableroService.actualizarTablero(10L, request);

        assertNotNull(result);
        assertEquals("Updated Board", result.getTitulo());
        assertEquals("New desc", result.getDescripcion());
    }

    @Test
    public void testArchivarTablero_Exitoso() {
        when(tableroRepository.findById(10L)).thenReturn(Optional.of(tablero));
        when(tableroRepository.save(any(Tablero.class))).thenAnswer(inv -> inv.getArgument(0));

        TableroDTO result = tableroService.archivarTablero(10L, true);

        assertNotNull(result);
        assertTrue(result.isArchivado());
    }

    @Test
    public void testEliminarTablero_Exitoso() {
        when(tableroRepository.existsById(10L)).thenReturn(true);

        tableroService.eliminarTablero(10L);

        verify(tableroRepository, times(1)).deleteById(10L);
    }

    @Test
    public void testEliminarTablero_NoEncontrado() {
        when(tableroRepository.existsById(10L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> tableroService.eliminarTablero(10L));
    }
}
