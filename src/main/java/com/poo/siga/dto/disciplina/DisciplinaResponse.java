package com.poo.siga.dto.disciplina;

import com.poo.siga.model.Disciplina;

public record DisciplinaResponse(
    Integer id, String codigo, String descricao, Integer cargaHorariaAnual, boolean ativo
) {
    public static DisciplinaResponse from(Disciplina d) {
        return new DisciplinaResponse(d.getId(), d.getCodigo(), d.getDescricao(), d.getCargaHorariaAnual(), d.isAtivo());
    }
}
