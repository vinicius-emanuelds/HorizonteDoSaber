package com.poo.siga.repository;

import com.poo.siga.model.AnoLetivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnoLetivoRepository extends JpaRepository<AnoLetivo, Integer> {

    Optional<AnoLetivo> findByAno(Integer ano);

    /** Retorna o ano letivo ativo (não encerrado) mais recente */
    Optional<AnoLetivo> findFirstByEncerradoFalseOrderByAnoDesc();

    /** Retorna todos os anos em ordem decrescente */
    List<AnoLetivo> findAllByOrderByAnoDesc();
}
