package com.poo.siga.dto.turma;

import com.poo.siga.model.enums.Turno;
import jakarta.validation.constraints.*;

public record TurmaRequest(
    @NotNull Integer anoLetivo,
    @NotNull @Min(1) @Max(5) Integer serie,
    @NotBlank String nome,
    @NotNull Turno turno,
    @NotNull Integer professorRegenteId
) {}
