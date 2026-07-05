package com.gestortareas.api.mensajeria.repository;

import com.gestortareas.api.mensajeria.entity.Conversacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversacionRepository extends JpaRepository<Conversacion, Long> {
}
