package com.gestortareas.api.etiqueta.service;

import com.gestortareas.api.etiqueta.dto.EtiquetaDTO;
import com.gestortareas.api.etiqueta.dto.EtiquetaRequest;
import com.gestortareas.api.etiqueta.entity.Etiqueta;
import com.gestortareas.api.etiqueta.repository.EtiquetaRepository;
import com.gestortareas.api.exceptions.EntityNotFoundException;
import com.gestortareas.api.tablero.entity.Tablero;
import com.gestortareas.api.tablero.repository.TableroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EtiquetaServiceImplTest {

    @Mock
    private EtiquetaRepository etiquetaRepository;

    @Mock
    private TableroRepository tableroRepository;

    @InjectMocks
    private EtiquetaServiceImpl etiquetaService;

    private Tablero tablero;
    private Etiqueta etiqueta;

    @BeforeEach
    public void setup() {
        tablero = Tablero.builder().id(1L).titulo("Tablero Test").build();
        etiqueta = Etiqueta.builder()
                .id(10L)
                .nombre("Bug")
                .color("#bfdbfe")
                .colorTexto("#1e3a5f")
                .tablero(tablero)
                .build();
    }

    @Test
    public void testListarPorTableroOGlobales() {
        when(etiquetaRepository.findGlobalesYPorTablero(1L)).thenReturn(List.of(etiqueta));

        List<EtiquetaDTO> result = etiquetaService.listarPorTableroOGlobales(1L);

        assertEquals(1, result.size());
        assertEquals("Bug", result.get(0).getNombre());
        assertEquals(10L, result.get(0).getId());
    }

    @Test
    public void testListarGlobales() {
        etiqueta.setTablero(null);
        when(etiquetaRepository.findByTableroIsNull()).thenReturn(List.of(etiqueta));

        List<EtiquetaDTO> result = etiquetaService.listarGlobales();

        assertEquals(1, result.size());
        assertNull(result.get(0).getTableroId());
    }

    @Test
    public void testCrearEtiqueta_Exitoso() {
        EtiquetaRequest request = new EtiquetaRequest();
        request.setNombre("Feature");
        request.setColor("#ff0000");
        request.setColorTexto("#ffffff");
        request.setTableroId(1L);

        when(tableroRepository.findById(1L)).thenReturn(Optional.of(tablero));
        when(etiquetaRepository.save(any(Etiqueta.class))).thenAnswer(inv -> {
            Etiqueta e = inv.getArgument(0);
            e.setId(11L);
            return e;
        });

        EtiquetaDTO result = etiquetaService.crearEtiqueta(request);

        assertNotNull(result);
        assertEquals(11L, result.getId());
        assertEquals("Feature", result.getNombre());
        assertEquals(1L, result.getTableroId());
    }

    @Test
    public void testCrearEtiqueta_TableroNoEncontrado() {
        EtiquetaRequest request = new EtiquetaRequest();
        request.setNombre("Feature");
        request.setColor("#ff0000");
        request.setColorTexto("#ffffff");
        request.setTableroId(1L);
        when(tableroRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> etiquetaService.crearEtiqueta(request));
    }

    @Test
    public void testActualizarEtiqueta_Exitoso() {
        EtiquetaRequest request = new EtiquetaRequest();
        request.setNombre("Feature Mod");
        request.setColor("#00ff00");
        request.setColorTexto("#000000");

        when(etiquetaRepository.findById(10L)).thenReturn(Optional.of(etiqueta));
        when(etiquetaRepository.save(any(Etiqueta.class))).thenAnswer(inv -> inv.getArgument(0));

        EtiquetaDTO result = etiquetaService.actualizarEtiqueta(10L, request);

        assertNotNull(result);
        assertEquals("Feature Mod", result.getNombre());
        assertEquals("#00ff00", result.getColor());
        assertEquals("#000000", result.getColorTexto());
    }

    @Test
    public void testActualizarEtiqueta_NoEncontrada() {
        EtiquetaRequest request = new EtiquetaRequest();
        request.setNombre("Feature Mod");
        request.setColor("#00ff00");
        request.setColorTexto("#000000");
        when(etiquetaRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> etiquetaService.actualizarEtiqueta(10L, request));
    }

    @Test
    public void testEliminarEtiqueta_Exitoso() {
        when(etiquetaRepository.existsById(10L)).thenReturn(true);

        etiquetaService.eliminarEtiqueta(10L);

        verify(etiquetaRepository, times(1)).deleteById(10L);
    }

    @Test
    public void testEliminarEtiqueta_NoEncontrada() {
        when(etiquetaRepository.existsById(10L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> etiquetaService.eliminarEtiqueta(10L));
    }
}
