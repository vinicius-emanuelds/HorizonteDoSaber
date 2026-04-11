package com.poo.siga.repository;

import com.poo.siga.model.TurmaDisciplinaProfessor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TurmaDisciplinaProfessorRepository extends JpaRepository<TurmaDisciplinaProfessor, Integer> {

    List<TurmaDisciplinaProfessor> findByTurmaId(Integer turmaId);
    List<TurmaDisciplinaProfessor> findByProfessorId(Integer professorId);
    boolean existsByDisciplinaIdAndTurmaId(Integer disciplinaId, Integer turmaId);
}
