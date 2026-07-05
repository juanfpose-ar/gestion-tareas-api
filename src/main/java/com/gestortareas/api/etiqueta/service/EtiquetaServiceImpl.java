package com.gestortareas.api.etiqueta.service;

import com.gestortareas.api.etiqueta.dto.EtiquetaDTO;
import com.gestortareas.api.etiqueta.dto.EtiquetaRequest;
import com.gestortareas.api.etiqueta.entity.Etiqueta;
import com.gestortareas.api.etiqueta.repository.EtiquetaRepository;
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
public class EtiquetaServiceImpl implements EtiquetaService {

    private final EtiquetaRepository etiquetaRepository;
    private final TableroRepository tableroRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EtiquetaDTO> listarPorTableroOGlobales(Long tableroId) {
        return etiquetaRepository.findGlobalesYPorTablero(tableroId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EtiquetaDTO> listarGlobales() {
        return etiquetaRepository.findByTableroIsNull().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EtiquetaDTO crearEtiqueta(EtiquetaRequest request) {
        Tablero tablero = null;
        if (request.getTableroId() != null) {
            tablero = tableroRepository.findById(request.getTableroId())
                    .orElseThrow(() -> new EntityNotFoundException("Tablero no encontrado con ID: " + request.getTableroId()));
        }

        Etiqueta etiqueta = Etiqueta.builder()
                .nombre(request.getNombre())
                .color(request.getColor() != null ? request.getColor() : "#bfdbfe")
                .colorTexto(request.getColorTexto() != null ? request.getColorTexto() : "#1e3a5f")
                .tablero(tablero)
                .build();

        Etiqueta saved = etiquetaRepository.save(etiqueta);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public EtiquetaDTO actualizarEtiqueta(Long id, EtiquetaRequest request) {
        Etiqueta etiqueta = etiquetaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Etiqueta no encontrada con ID: " + id));

        etiqueta.setNombre(request.getNombre());
        if (request.getColor() != null) {
            etiqueta.setColor(request.getColor());
        }
        if (request.getColorTexto() != null) {
            etiqueta.setColorTexto(request.getColorTexto());
        }

        Etiqueta updated = etiquetaRepository.save(etiqueta);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void eliminarEtiqueta(Long id) {
        if (!etiquetaRepository.existsById(id)) {
            throw new EntityNotFoundException("Etiqueta no encontrada con ID: " + id);
        }
        etiquetaRepository.deleteById(id);
    }

    private EtiquetaDTO mapToDTO(Etiqueta e) {
        return EtiquetaDTO.builder()
                .id(e.getId())
                .nombre(e.getNombre())
                .color(e.getColor())
                .colorHex(e.getColor())
                .colorTexto(e.getColorTexto() != null ? e.getColorTexto() : "#1e293b")
                .tableroId(e.getTablero() != null ? e.getTablero().getId() : null)
                .build();
    }
}

