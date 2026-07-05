package com.gestortareas.api.tablero.repository;

import com.gestortareas.api.tablero.entity.Tablero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TableroRepository extends JpaRepository<Tablero, Long> {

    @Query("SELECT DISTINCT t FROM Tablero t JOIN Usuario u ON t MEMBER OF u.tablerosAsignados WHERE u.username = :username")
    List<Tablero> findTablerosAsignadosAUsuario(@Param("username") String username);
}
