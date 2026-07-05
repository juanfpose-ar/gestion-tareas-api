package com.gestortareas.api.mensajeria.repository;

import com.gestortareas.api.mensajeria.entity.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
    List<Mensaje> findByConversacionIdOrderByFechaEnvioAsc(Long conversacionId);
    Mensaje findFirstByConversacionIdOrderByFechaEnvioDesc(Long conversacionId);
}
