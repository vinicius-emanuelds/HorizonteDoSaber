package com.poo.siga.model.enums;

public enum EspecialidadeProfessor {
    REGENTE("Regente"),
    ARTES("Artes"),
    EDUCACAO_FISICA("Educação Física"),
    INFORMATICA("Informática"),
    OUTROS("Outros");

    private final String descricao;

    EspecialidadeProfessor(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
