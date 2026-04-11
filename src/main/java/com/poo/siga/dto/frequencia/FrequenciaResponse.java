package com.poo.siga.dto.frequencia;

import com.poo.siga.model.Frequencia;
import java.time.LocalDate;

public record FrequenciaResponse(
    Integer id, Integer turmaId, Integer disciplinaId, String disciplinaNome,
    Integer alunoId, String alunoNome, LocalDate data,
    String status, String justificativa
) {
    public static FrequenciaResponse from(Frequencia f) {
        return new FrequenciaResponse(
            f.getId(), f.getTurma().getId(), f.getDisciplina().getId(),
            f.getDisciplina().getDescricao(), f.getAluno().getId(), f.getAluno().getNome(),
            f.getData(), f.getStatus().name(), f.getJustificativa()
        );
    }
}
