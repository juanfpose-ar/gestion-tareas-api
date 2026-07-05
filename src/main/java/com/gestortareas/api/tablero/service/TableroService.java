package com.gestortareas.api.tablero.service;

import com.gestortareas.api.tablero.dto.TableroDTO;
import com.gestortareas.api.tablero.dto.TableroRequest;

import java.util.List;

public interface TableroService {
    List<TableroDTO> listarVisiblesParaUsuario(String username, boolean esAdmin);
    List<TableroDTO> listarTodosParaAdmin();
    TableroDTO obtenerPorId(Long id);
    TableroDTO crearTablero(TableroRequest request);
    TableroDTO actualizarTablero(Long id, TableroRequest request);
    TableroDTO archivarTablero(Long id, boolean archivado);
    void eliminarTablero(Long id);
}
