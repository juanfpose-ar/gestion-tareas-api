package com.gestortareas.api.version.repository;

import com.gestortareas.api.version.entity.Version;
import com.gestortareas.api.version.entity.VersionEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VersionRepository extends JpaRepository<Version, Long> {
    List<Version> findByTableroIdOrderByFechaVencimientoAsc(Long tableroId);
    boolean existsByTableroIdAndEstado(Long tableroId, VersionEstado estado);
    boolean existsByTableroIdAndEstadoAndIdNot(Long tableroId, VersionEstado estado, Long id);

    @Query("SELECT v.tablero.id FROM Version v WHERE v.id = :versionId")
    Optional<Long> findTableroIdByVersionId(@Param("versionId") Long versionId);
}
