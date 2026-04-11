package com.poo.siga.dto.matricula;

import jakarta.validation.constraints.NotNull;

public record MatriculaRequest(
    @NotNull Integer alunoId,
    @NotNull Integer turmaId
) {}
