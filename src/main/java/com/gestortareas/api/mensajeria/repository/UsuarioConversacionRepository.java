package com.gestortareas.api.mensajeria.repository;

import com.gestortareas.api.mensajeria.entity.UsuarioConversacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioConversacionRepository extends JpaRepository<UsuarioConversacion, Long> {
    
    Optional<UsuarioConversacion> findByUsuarioIdAndConversacionId(Long usuarioId, Long conversacionId);

    @Query("SELECT uc FROM UsuarioConversacion uc JOIN uc.conversacion c " +
           "WHERE uc.usuario.id = :usuarioId AND uc.eliminada = false " +
           "ORDER BY c.fechaUltimaActividad DESC")
    List<UsuarioConversacion> findActiveConversationsForUser(@Param("usuarioId") Long usuarioId);
}
