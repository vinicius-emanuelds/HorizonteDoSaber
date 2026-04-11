package com.poo.siga.repository;

import com.poo.siga.model.Disciplina;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DisciplinaRepository extends JpaRepository<Disciplina, Integer> {

    @Query("SELECT d FROM Disciplina d WHERE " +
           "(cast(:descricao as String) IS NULL OR LOWER(d.descricao) LIKE LOWER(CONCAT('%', cast(:descricao as String), '%'))) AND " +
           "(cast(:codigo as String) IS NULL OR d.codigo = :codigo) AND " +
           "(:ativo IS NULL OR d.ativo = :ativo)")
    Page<Disciplina> buscarComFiltros(
        @Param("descricao") String descricao,
        @Param("codigo") String codigo,
        @Param("ativo") Boolean ativo,
        Pageable pageable
    );
}