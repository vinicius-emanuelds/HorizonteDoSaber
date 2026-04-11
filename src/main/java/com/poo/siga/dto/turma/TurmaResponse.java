package com.poo.siga.dto.turma;

import com.poo.siga.model.Turma;

public record TurmaResponse(
    Integer id, String codigo, Integer anoLetivo, Integer serie, String nome,
    String turno, Integer professorRegenteId, String professorRegenteNome,
    boolean ativo, String identificacao
) {
    public static TurmaResponse from(Turma t) {
        return new TurmaResponse(
            t.getId(), t.getCodigo(), t.getAnoLetivo(), t.getSerie(), t.getNome(),
            t.getTurno().name(),
            t.getProfessorRegente() != null ? t.getProfessorRegente().getId() : null,
            t.getProfessorRegente() != null ? t.getProfessorRegente().getNome() : null,
            t.isAtivo(), t.getIdentificacao()
        );
    }
}
