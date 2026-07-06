package com.gestortareas.api.reunion.repository;

import com.gestortareas.api.reunion.entity.Reunion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReunionRepository extends JpaRepository<Reunion, Long> {
    List<Reunion> findByTableroIdOrderByFechaAscHoraInicioAsc(Long tableroId);
    List<Reunion> findByFechaBetween(LocalDate desde, LocalDate hasta);

    @Query("SELECT r.tablero.id FROM Reunion r WHERE r.id = :reunionId")
    Optional<Long> findTableroIdByReunionId(@Param("reunionId") Long reunionId);
}
