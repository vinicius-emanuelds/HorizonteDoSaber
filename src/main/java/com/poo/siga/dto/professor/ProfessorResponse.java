package com.poo.siga.dto.professor;

import com.poo.siga.model.Professor;
import java.time.LocalDate;

public record ProfessorResponse(
    Integer id, String codigoFuncional, String nome, LocalDate dataNascimento,
    String cpf, String email, boolean ativo,
    String especialidade, String especialidadeDescricao
) {
    public static ProfessorResponse from(Professor p) {
        return new ProfessorResponse(p.getId(), p.getCodigoFuncional(), p.getNome(),
            p.getDataNascimento(), p.getCpf(), p.getEmail(), p.isAtivo(),
            p.getEspecialidade() != null ? p.getEspecialidade().name() : null,
            p.getEspecialidade() != null ? p.getEspecialidade().getDescricao() : null);
    }
}
