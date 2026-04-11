package com.poo.siga.dto.matricula;

import com.poo.siga.model.Matricula;
import java.time.LocalDate;

public record MatriculaResponse(
    Integer id, String numero, Integer alunoId, String alunoNome, String alunoRa,
    Integer turmaId, String turmaIdentificacao, Integer anoLetivo, Integer serie,
    String turno, LocalDate dataMatricula, String situacao
) {
    public static MatriculaResponse from(Matricula m) {
        return new MatriculaResponse(
            m.getId(), m.getNumero(),
            m.getAluno().getId(), m.getAluno().getNome(), m.getAluno().getRa(),
            m.getTurma().getId(), m.getTurma().getIdentificacao(),
            m.getAnoLetivo(), m.getSerie(), m.getTurno().name(),
            m.getDataMatricula(), m.getSituacao().name()
        );
    }
}
