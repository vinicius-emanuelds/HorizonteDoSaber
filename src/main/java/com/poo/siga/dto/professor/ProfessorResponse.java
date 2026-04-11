package com.poo.siga.dto.professor;

import com.poo.siga.model.Professor;
import java.time.LocalDate;

public record ProfessorResponse(
    Integer id, String codigoFuncional, String nome, LocalDate dataNascimento,
    String cpf, String email, boolean ativo
) {
    public static ProfessorResponse from(Professor p) {
        return new ProfessorResponse(p.getId(), p.getCodigoFuncional(), p.getNome(),
            p.getDataNascimento(), p.getCpf(), p.getEmail(), p.isAtivo());
    }
}
