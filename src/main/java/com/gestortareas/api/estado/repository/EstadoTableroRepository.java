package com.gestortareas.api.estado.repository;

import com.gestortareas.api.estado.entity.EstadoTablero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EstadoTableroRepository extends JpaRepository<EstadoTablero, Long> {
    List<EstadoTablero> findByTableroIdOrderByOrdenAsc(Long tableroId);
}
