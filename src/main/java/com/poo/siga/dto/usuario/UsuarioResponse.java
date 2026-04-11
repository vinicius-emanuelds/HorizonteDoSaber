package com.poo.siga.dto.usuario;

import com.poo.siga.model.Usuario;
import java.time.LocalDateTime;

public record UsuarioResponse(
    Integer id, String codigo, String nomeCompleto, String email, String login,
    String role, LocalDateTime dataCadastro, boolean ativo, boolean bloqueado,
    boolean primeiroAcesso, Integer professorId
) {
    public static UsuarioResponse from(Usuario u) {
        return new UsuarioResponse(
            u.getId(), u.getCodigo(), u.getNomeCompleto(), u.getEmail(), u.getLogin(),
            u.getRole().name(), u.getDataCadastro(), u.isAtivo(), u.isBloqueado(),
            u.isPrimeiroAcesso(),
            u.getProfessor() != null ? u.getProfessor().getId() : null
        );
    }
}
