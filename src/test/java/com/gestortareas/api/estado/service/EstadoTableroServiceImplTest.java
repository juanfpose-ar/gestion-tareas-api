package com.gestortareas.api.estado.service;

import com.gestortareas.api.estado.dto.EstadoTableroDTO;
import com.gestortareas.api.estado.dto.EstadoTableroRequest;
import com.gestortareas.api.estado.entity.EstadoTablero;
import com.gestortareas.api.estado.repository.EstadoTableroRepository;
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
public class EstadoTableroServiceImplTest {

    @Mock
    private EstadoTableroRepository estadoRepository;

    @Mock
    private TableroRepository tableroRepository;

    @InjectMocks
    private EstadoTableroServiceImpl estadoService;

    private Tablero tablero;
    private EstadoTablero estado;

    @BeforeEach
    public void setup() {
        tablero = Tablero.builder().id(1L).titulo("Tablero Test").build();
        estado = EstadoTablero.builder().id(10L).nombre("ToDo").orden(1).colorHex("#ffffff").tablero(tablero).build();
    }

    @Test
    public void testListarPorTablero() {
        when(estadoRepository.findByTableroIdOrderByOrdenAsc(1L)).thenReturn(List.of(estado));

        List<EstadoTableroDTO> result = estadoService.listarPorTablero(1L);

        assertEquals(1, result.size());
        assertEquals("ToDo", result.get(0).getNombre());
        assertEquals(10L, result.get(0).getId());
    }

    @Test
    public void testCrearEstado_Exitoso() {
        EstadoTableroRequest request = new EstadoTableroRequest();
        request.setNombre("ToDo");
        request.setOrden(1);
        request.setColorHex("#ffffff");
        request.setTableroId(1L);

        when(tableroRepository.findById(1L)).thenReturn(Optional.of(tablero));
        when(estadoRepository.save(any(EstadoTablero.class))).thenAnswer(inv -> {
            EstadoTablero et = inv.getArgument(0);
            et.setId(10L);
            return et;
        });

        EstadoTableroDTO result = estadoService.crearEstado(request);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("ToDo", result.getNombre());
        assertEquals("#ffffff", result.getColorHex());
    }

    @Test
    public void testCrearEstado_TableroNoEncontrado() {
        EstadoTableroRequest request = new EstadoTableroRequest();
        request.setNombre("ToDo");
        request.setOrden(1);
        request.setColorHex("#ffffff");
        request.setTableroId(1L);
        when(tableroRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> estadoService.crearEstado(request));
    }

    @Test
    public void testActualizarEstado_Exitoso() {
        EstadoTableroRequest request = new EstadoTableroRequest();
        request.setNombre("In Progress");
        request.setOrden(2);
        request.setColorHex("#000000");
        request.setTableroId(1L);

        when(estadoRepository.findById(10L)).thenReturn(Optional.of(estado));
        when(estadoRepository.save(any(EstadoTablero.class))).thenAnswer(inv -> inv.getArgument(0));

        EstadoTableroDTO result = estadoService.actualizarEstado(10L, request);

        assertNotNull(result);
        assertEquals("In Progress", result.getNombre());
        assertEquals(2, result.getOrden());
        assertEquals("#000000", result.getColorHex());
    }

    @Test
    public void testActualizarEstado_NoEncontrado() {
        EstadoTableroRequest request = new EstadoTableroRequest();
        request.setNombre("In Progress");
        request.setOrden(2);
        request.setColorHex("#000000");
        request.setTableroId(1L);
        when(estadoRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> estadoService.actualizarEstado(10L, request));
    }

    @Test
    public void testEliminarEstado_Exitoso() {
        when(estadoRepository.existsById(10L)).thenReturn(true);

        estadoService.eliminarEstado(10L);

        verify(estadoRepository, times(1)).deleteById(10L);
    }

    @Test
    public void testEliminarEstado_NoEncontrado() {
        when(estadoRepository.existsById(10L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> estadoService.eliminarEstado(10L));
    }
}
