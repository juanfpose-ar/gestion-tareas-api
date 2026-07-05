package com.gestortareas.api.estado.service;

import com.gestortareas.api.estado.dto.EstadoTableroDTO;
import com.gestortareas.api.estado.dto.EstadoTableroRequest;
import com.gestortareas.api.estado.entity.EstadoTablero;
import com.gestortareas.api.estado.repository.EstadoTableroRepository;
import com.gestortareas.api.exceptions.EntityNotFoundException;
import com.gestortareas.api.tablero.entity.Tablero;
import com.gestortareas.api.tablero.repository.TableroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EstadoTableroServiceImpl implements EstadoTableroService {

    private final EstadoTableroRepository estadoRepository;
    private final TableroRepository tableroRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EstadoTableroDTO> listarPorTablero(Long tableroId) {
        return estadoRepository.findByTableroIdOrderByOrdenAsc(tableroId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EstadoTableroDTO crearEstado(EstadoTableroRequest request) {
        Tablero tablero = tableroRepository.findById(request.getTableroId())
                .orElseThrow(() -> new EntityNotFoundException("Tablero no encontrado con ID: " + request.getTableroId()));

        EstadoTablero estado = EstadoTablero.builder()
                .nombre(request.getNombre())
                .orden(request.getOrden())
                .colorHex(request.getColorHex() != null ? request.getColorHex() : "#6b7280")
                .tablero(tablero)
                .build();

        EstadoTablero saved = estadoRepository.save(estado);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public EstadoTableroDTO actualizarEstado(Long id, EstadoTableroRequest request) {
        EstadoTablero estado = estadoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Estado no encontrado con ID: " + id));

        estado.setNombre(request.getNombre());
        estado.setOrden(request.getOrden());
        if (request.getColorHex() != null) {
            estado.setColorHex(request.getColorHex());
        }

        EstadoTablero updated = estadoRepository.save(estado);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void eliminarEstado(Long id) {
        if (!estadoRepository.existsById(id)) {
            throw new EntityNotFoundException("Estado no encontrado con ID: " + id);
        }
        estadoRepository.deleteById(id);
    }

    private EstadoTableroDTO mapToDTO(EstadoTablero e) {
        return EstadoTableroDTO.builder()
                .id(e.getId())
                .nombre(e.getNombre())
                .orden(e.getOrden())
                .colorHex(e.getColorHex())
                .tableroId(e.getTablero().getId())
                .build();
    }
}
