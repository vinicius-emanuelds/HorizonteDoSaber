package com.poo.siga.repository;

import com.poo.siga.model.GradeHoraria;
import com.poo.siga.model.Turma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface GradeHorariaRepository extends JpaRepository<GradeHoraria, Integer> {
    List<GradeHoraria> findByTurmaId(Integer turmaId);
    List<GradeHoraria> findByTurmaIdAndDiaSemana(Integer turmaId, DayOfWeek diaSemana);
    Optional<GradeHoraria> findByTurmaIdAndDiaSemanaAndNumeroAula(Integer turmaId, DayOfWeek diaSemana, Integer numeroAula);
    void deleteByTurma(Turma turma);

    @Query("SELECT DISTINCT tdp.disciplina.id FROM TurmaDisciplinaProfessor tdp " +
           "WHERE tdp.turma.id = :turmaId " +
           "AND tdp.professor.id IN (" +
           "  SELECT u.professor.id FROM Usuario u WHERE u.login = :login AND u.professor IS NOT NULL" +
           ")")
    Set<Integer> findDisciplinaIdsByTurmaAndProfessorLogin(
        @Param("turmaId") Integer turmaId,
        @Param("login") String login
    );
}
