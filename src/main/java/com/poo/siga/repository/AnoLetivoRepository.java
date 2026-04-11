package com.poo.siga.repository;

import com.poo.siga.model.AnoLetivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnoLetivoRepository extends JpaRepository<AnoLetivo, Integer> {

    Optional<AnoLetivo> findByAno(Integer ano);
    Optional<AnoLetivo> findByEncerradoFalse();
}
