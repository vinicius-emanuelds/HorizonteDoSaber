package com.poo.siga.repository;

import com.poo.siga.model.Usuario;
import com.poo.siga.model.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByLogin(String login);
    boolean existsByLogin(String login);

    @Query("SELECT u FROM Usuario u WHERE " +
           "(cast(:nome as String) IS NULL OR LOWER(u.nomeCompleto) LIKE LOWER(CONCAT('%', cast(:nome as String), '%'))) AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:ativo IS NULL OR u.ativo = :ativo)")
    Page<Usuario> buscarComFiltros(
        @Param("nome") String nome,
        @Param("role") Role role,
        @Param("ativo") Boolean ativo,
        Pageable pageable
    );

    long countByRole(Role role);
}
