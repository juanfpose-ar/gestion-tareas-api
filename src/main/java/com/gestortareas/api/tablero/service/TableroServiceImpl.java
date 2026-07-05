package com.gestortareas.api.tablero.service;

import com.gestortareas.api.estado.entity.EstadoTablero;
import com.gestortareas.api.estado.repository.EstadoTableroRepository;
import com.gestortareas.api.exceptions.EntityNotFoundException;
import com.gestortareas.api.tablero.dto.TableroDTO;
import com.gestortareas.api.tablero.dto.TableroRequest;
import com.gestortareas.api.tablero.entity.Tablero;
import com.gestortareas.api.tablero.repository.TableroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TableroServiceImpl implements TableroService {

    private final TableroRepository tableroRepository;
    private final EstadoTableroRepository estadoTableroRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TableroDTO> listarVisiblesParaUsuario(String username, boolean esAdmin) {
        if (esAdmin) {
            return tableroRepository.findAll().stream()
                    .filter(t -> !t.isArchivado())
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        }
        return tableroRepository.findTablerosAsignadosAUsuario(username).stream()
                .filter(t -> !t.isArchivado())
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TableroDTO> listarTodosParaAdmin() {
        return tableroRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TableroDTO obtenerPorId(Long id) {
        Tablero tablero = tableroRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tablero no encontrado con ID: " + id));
        return mapToDTO(tablero);
    }

    @Override
    @Transactional
    public TableroDTO crearTablero(TableroRequest request) {
        Tablero tablero = Tablero.builder()
                .titulo(request.getTitulo())
                .descripcion(request.getDescripcion())
                .imagenFondoUrl(request.getImagenFondoUrl())
                .build();

        Tablero saved = tableroRepository.save(tablero);

        estadoTableroRepository.save(EstadoTablero.builder().nombre("Por Hacer").orden(1).colorHex("#f59e0b").tablero(saved).build());
        estadoTableroRepository.save(EstadoTablero.builder().nombre("En Proceso").orden(2).colorHex("#3b82f6").tablero(saved).build());
        estadoTableroRepository.save(EstadoTablero.builder().nombre("Completado").orden(3).colorHex("#10b981").tablero(saved).build());

        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public TableroDTO actualizarTablero(Long id, TableroRequest request) {
        Tablero tablero = tableroRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tablero no encontrado con ID: " + id));
        tablero.setTitulo(request.getTitulo());
        tablero.setDescripcion(request.getDescripcion());
        tablero.setImagenFondoUrl(request.getImagenFondoUrl());
        return mapToDTO(tableroRepository.save(tablero));
    }

    @Override
    @Transactional
    public TableroDTO archivarTablero(Long id, boolean archivado) {
        Tablero tablero = tableroRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tablero no encontrado con ID: " + id));
        tablero.setArchivado(archivado);
        return mapToDTO(tableroRepository.save(tablero));
    }

    @Override
    @Transactional
    public void eliminarTablero(Long id) {
        if (!tableroRepository.existsById(id)) {
            throw new EntityNotFoundException("Tablero no encontrado con ID: " + id);
        }
        tableroRepository.deleteById(id);
    }

    private TableroDTO mapToDTO(Tablero t) {
        return TableroDTO.builder()
                .id(t.getId())
                .titulo(t.getTitulo())
                .descripcion(t.getDescripcion())
                .imagenFondoUrl(t.getImagenFondoUrl())
                .fechaCreacion(t.getFechaCreacion())
                .archivado(t.isArchivado())
                .build();
    }
}
