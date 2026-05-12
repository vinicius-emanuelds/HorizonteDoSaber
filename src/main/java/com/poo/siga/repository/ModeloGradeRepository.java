package com.poo.siga.repository;

import com.poo.siga.model.ModeloGrade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModeloGradeRepository extends JpaRepository<ModeloGrade, Integer> {
    List<ModeloGrade> findByAnoLetivoAndSerie(Integer anoLetivo, Integer serie);
    List<ModeloGrade> findByAnoLetivoOrderBySerieAscNomeAsc(Integer anoLetivo);
}
