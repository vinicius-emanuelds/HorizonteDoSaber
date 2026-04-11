package com.poo.siga.repository;

import com.poo.siga.model.Nota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotaRepository extends JpaRepository<Nota, Integer> {

    List<Nota> findByTurmaIdAndDisciplinaIdAndPeriodo(Integer turmaId, Integer disciplinaId, Integer periodo);
    List<Nota> findByTurmaIdAndAlunoId(Integer turmaId, Integer alunoId);
    List<Nota> findByAlunoIdAndDisciplinaId(Integer alunoId, Integer disciplinaId);
    List<Nota> findByAlunoId(Integer alunoId);
    List<Nota> findByTurmaId(Integer turmaId);

    @Query("SELECT n FROM Nota n WHERE n.turma.id = :turmaId AND n.disciplina.id = :disciplinaId " +
           "AND n.aluno.id = :alunoId AND n.periodo = :periodo")
    List<Nota> buscarNotasDoPeriodo(
        @Param("turmaId") Integer turmaId,
        @Param("disciplinaId") Integer disciplinaId,
        @Param("alunoId") Integer alunoId,
        @Param("periodo") Integer periodo
    );
}
