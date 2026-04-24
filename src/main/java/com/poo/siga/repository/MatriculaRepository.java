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

    /** Retorna o maior número seq. de matrícula do ano (ex: "MAT202600015" -> 15). Null se vazio. */
    @Query("SELECT MAX(CAST(SUBSTRING(m.numero, 8) AS integer)) FROM Matricula m WHERE m.numero LIKE CONCAT('MAT', :ano, '%')")
    Integer findMaxNumeroByAno(@Param("ano") String ano);

    @Query("SELECT m FROM Matricula m WHERE " +
           "(cast(:nomeAluno as String) IS NULL OR LOWER(m.aluno.nome) LIKE LOWER(CONCAT('%', cast(:nomeAluno as String), '%'))) AND " +
           "(:turmaId IS NULL OR m.turma.id = :turmaId) AND " +
           "(:anoLetivo IS NULL OR m.anoLetivo = :anoLetivo) AND " +
           "(:situacao IS NULL OR m.situacao = :situacao)")
    Page<Matricula> buscarComFiltros(
        @Param("nomeAluno") String nomeAluno,
        @Param("turmaId") Integer turmaId,
        @Param("anoLetivo") Integer anoLetivo,
        @Param("situacao") SituacaoMatricula situacao,
        Pageable pageable
    );

    long countByTurmaId(Integer turmaId);
}
