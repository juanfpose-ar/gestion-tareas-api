package com.gestortareas.api.reunion.repository;

import com.gestortareas.api.reunion.entity.Reunion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReunionRepository extends JpaRepository<Reunion, Long> {
    List<Reunion> findByTableroIdOrderByFechaAscHoraInicioAsc(Long tableroId);
    List<Reunion> findByFechaBetween(LocalDate desde, LocalDate hasta);
}
