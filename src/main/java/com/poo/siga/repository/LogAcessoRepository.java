package com.poo.siga.repository;

import com.poo.siga.model.LogAcesso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogAcessoRepository extends JpaRepository<LogAcesso, Long> {
    long countByUsuario(String usuario);
}
