package com.poo.siga.dto.nota;

import com.poo.siga.model.Nota;
import java.time.LocalDateTime;

public record NotaResponse(
    Integer id, Integer turmaId, Integer disciplinaId, String disciplinaNome,
    Integer alunoId, String alunoNome, Integer periodo,
    String tipoAvaliacao, Double valor, LocalDateTime dataLancamento
) {
    public static NotaResponse from(Nota n) {
        return new NotaResponse(
            n.getId(), n.getTurma().getId(), n.getDisciplina().getId(),
            n.getDisciplina().getDescricao(), n.getAluno().getId(), n.getAluno().getNome(),
            n.getPeriodo(), n.getTipoAvaliacao().name(), n.getValor(), n.getDataLancamento()
        );
    }
}
