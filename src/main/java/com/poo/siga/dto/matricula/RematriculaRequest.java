package com.poo.siga.dto.matricula;

import jakarta.validation.constraints.NotNull;

public record RematriculaRequest(
    @NotNull Integer turmaOrigemId,
    @NotNull Integer turmaDestinoId
) {}
