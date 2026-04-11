package com.poo.siga.dto.aluno;

import com.poo.siga.model.Aluno;
import java.time.LocalDate;

public record AlunoResponse(
    Integer id, String ra, String nome, LocalDate dataNascimento,
    String cpf, String email, String nomeResponsavel, String cpfResponsavel, boolean ativo
) {
    public static AlunoResponse from(Aluno a) {
        return new AlunoResponse(a.getId(), a.getRa(), a.getNome(), a.getDataNascimento(),
            a.getCpf(), a.getEmail(), a.getNomeResponsavel(), a.getCpfResponsavel(), a.isAtivo());
    }
}
