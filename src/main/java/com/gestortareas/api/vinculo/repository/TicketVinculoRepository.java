package com.gestortareas.api.vinculo.repository;

import com.gestortareas.api.vinculo.entity.TicketVinculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketVinculoRepository extends JpaRepository<TicketVinculo, Long> {

    @Query("SELECT v FROM TicketVinculo v WHERE v.ticketOrigen.id = :ticketId OR v.ticketDestino.id = :ticketId")
    List<TicketVinculo> findAllByTicketId(@Param("ticketId") Long ticketId);

    boolean existsByTicketOrigenIdAndTicketDestinoIdAndTipoVinculo(Long origenId, Long destinoId, String tipoVinculo);
}
