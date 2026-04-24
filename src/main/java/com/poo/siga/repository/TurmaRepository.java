package com.poo.siga.repository;

import com.poo.siga.model.Turma;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TurmaRepository extends JpaRepository<Turma, Integer> {

    List<Turma> findByAnoLetivo(Integer anoLetivo);
    List<Turma> findByAnoLetivoAndAtivo(Integer anoLetivo, boolean ativo);

    /** Retorna o maior número seq. do codigo (ex: "TUR00010" -> 10). Null se vazio. */
    @Query("SELECT MAX(CAST(SUBSTRING(t.codigo, 4) AS integer)) FROM Turma t WHERE t.codigo LIKE 'TUR%'")
    Integer findMaxCodigoNumber();

    @Query("SELECT t FROM Turma t WHERE " +
           "(:anoLetivo IS NULL OR t.anoLetivo = :anoLetivo) AND " +
           "(:serie IS NULL OR t.serie = :serie) AND " +
           "(:ativo IS NULL OR t.ativo = :ativo)")
    Page<Turma> buscarComFiltros(
        @Param("anoLetivo") Integer anoLetivo,
        @Param("serie") Integer serie,
        @Param("ativo") Boolean ativo,
        Pageable pageable
    );
}