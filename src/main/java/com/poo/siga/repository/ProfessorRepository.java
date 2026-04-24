package com.poo.siga.repository;

import com.poo.siga.model.Professor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfessorRepository extends JpaRepository<Professor, Integer> {

    boolean existsByCpf(String cpf);
    Optional<Professor> findByCodigoFuncional(String codigoFuncional);

    /** Retorna o maior número seq. do codigoFuncional (ex: "RF000015" -> 15). Null se vazio. */
    @Query("SELECT MAX(CAST(SUBSTRING(p.codigoFuncional, 3) AS integer)) FROM Professor p WHERE p.codigoFuncional LIKE 'RF%'")
    Integer findMaxCodigoFuncionalNumber();

    @Query("SELECT p FROM Professor p WHERE " +
           "(cast(:nome as String) IS NULL OR LOWER(p.nome) LIKE LOWER(CONCAT('%', cast(:nome as String), '%'))) AND " +
           "(cast(:codigoFuncional as String) IS NULL OR p.codigoFuncional = :codigoFuncional) AND " +
           "(:ativo IS NULL OR p.ativo = :ativo)")
    Page<Professor> buscarComFiltros(
        @Param("nome") String nome,
        @Param("codigoFuncional") String codigoFuncional,
        @Param("ativo") Boolean ativo,
        Pageable pageable
    );
}
