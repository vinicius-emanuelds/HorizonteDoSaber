package com.poo.siga.repository;

import com.poo.siga.model.Frequencia;
import com.poo.siga.model.enums.StatusFrequencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FrequenciaRepository extends JpaRepository<Frequencia, Integer> {

    List<Frequencia> findByTurmaIdAndDisciplinaIdAndData(Integer turmaId, Integer disciplinaId, LocalDate data);
    List<Frequencia> findByTurmaIdAndAlunoId(Integer turmaId, Integer alunoId);
    List<Frequencia> findByAlunoIdAndDisciplinaId(Integer alunoId, Integer disciplinaId);
    List<Frequencia> findByAlunoId(Integer alunoId);
    long countByAlunoIdAndDisciplinaIdAndStatus(Integer alunoId, Integer disciplinaId, StatusFrequencia status);
    long countByAlunoIdAndDisciplinaId(Integer alunoId, Integer disciplinaId);
}
