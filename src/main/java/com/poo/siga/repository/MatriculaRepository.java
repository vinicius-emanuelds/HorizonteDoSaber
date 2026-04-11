package com.poo.siga.repository;

import com.poo.siga.model.Matricula;
import com.poo.siga.model.enums.SituacaoMatricula;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatriculaRepository extends JpaRepository<Matricula, Integer> {

    List<Matricula> findByAlunoId(Integer alunoId);
    List<Matricula> findByTurmaId(Integer turmaId);
    boolean existsByAlunoIdAndAnoLetivoAndSituacao(Integer alunoId, Integer anoLetivo, SituacaoMatricula situacao);

    @Query("SELECT m FROM Matricula m WHERE " +
           "(:alunoId IS NULL OR m.aluno.id = :alunoId) AND " +
           "(:turmaId IS NULL OR m.turma.id = :turmaId) AND " +
           "(:anoLetivo IS NULL OR m.anoLetivo = :anoLetivo) AND " +
           "(:situacao IS NULL OR m.situacao = :situacao)")
    Page<Matricula> buscarComFiltros(
        @Param("alunoId") Integer alunoId,
        @Param("turmaId") Integer turmaId,
        @Param("anoLetivo") Integer anoLetivo,
        @Param("situacao") SituacaoMatricula situacao,
        Pageable pageable
    );

    long countByTurmaId(Integer turmaId);
}
