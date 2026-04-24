package com.poo.siga.repository;

import com.poo.siga.model.Aluno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlunoRepository extends JpaRepository<Aluno, Integer> {

    boolean existsByCpf(String cpf);
    boolean existsByEmail(String email);
    Optional<Aluno> findByRa(String ra);
    Optional<Aluno> findByCpf(String cpf);

    /**
     * Retorna o maior número seqüencial presente nos RAs cadastrados (ex: "RA000042" -> 42).
     * Retorna null se não houver nenhum aluno.
     */
    @Query("SELECT MAX(CAST(SUBSTRING(a.ra, 3) AS integer)) FROM Aluno a WHERE a.ra LIKE 'RA%'")
    Integer findMaxRaNumber();

    @Query("SELECT a FROM Aluno a WHERE " +
           "(cast(:nome as String) IS NULL OR LOWER(a.nome) LIKE LOWER(CONCAT('%', cast(:nome as String), '%'))) AND " +
           "(cast(:ra as String) IS NULL OR a.ra = :ra) AND " +
           "(cast(:cpf as String) IS NULL OR a.cpf = :cpf) AND " +
           "(:ativo IS NULL OR a.ativo = :ativo)")
    Page<Aluno> buscarComFiltros(
        @Param("nome") String nome,
        @Param("ra") String ra,
        @Param("cpf") String cpf,
        @Param("ativo") Boolean ativo,
        Pageable pageable
    );
}
