package com.gestortareas.api.etiqueta.repository;

import com.gestortareas.api.etiqueta.entity.Etiqueta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EtiquetaRepository extends JpaRepository<Etiqueta, Long> {
    
    @Query("SELECT e FROM Etiqueta e WHERE e.tablero IS NULL OR e.tablero.id = :tableroId")
    List<Etiqueta> findGlobalesYPorTablero(@Param("tableroId") Long tableroId);

    List<Etiqueta> findByTableroIsNull();
    List<Etiqueta> findByTableroId(Long tableroId);
}
