package com.gestortareas.api.version.repository;

import com.gestortareas.api.version.entity.Version;
import com.gestortareas.api.version.entity.VersionEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VersionRepository extends JpaRepository<Version, Long> {
    List<Version> findByTableroIdOrderByFechaVencimientoAsc(Long tableroId);
    boolean existsByTableroIdAndEstado(Long tableroId, VersionEstado estado);
    boolean existsByTableroIdAndEstadoAndIdNot(Long tableroId, VersionEstado estado, Long id);
}
