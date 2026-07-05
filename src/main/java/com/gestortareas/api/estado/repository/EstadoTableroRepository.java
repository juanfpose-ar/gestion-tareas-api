package com.gestortareas.api.estado.repository;

import com.gestortareas.api.estado.entity.EstadoTablero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstadoTableroRepository extends JpaRepository<EstadoTablero, Long> {
    List<EstadoTablero> findByTableroIdOrderByOrdenAsc(Long tableroId);

    @Query("SELECT e.tablero.id FROM EstadoTablero e WHERE e.id = :estadoId")
    Optional<Long> findTableroIdByEstadoId(@Param("estadoId") Long estadoId);
}
