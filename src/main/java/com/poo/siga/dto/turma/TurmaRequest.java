package com.poo.siga.dto.turma;

import com.poo.siga.model.enums.Turno;
import jakarta.validation.constraints.*;
import java.util.List;

public record TurmaRequest(
    @NotNull Integer anoLetivo,
    @NotNull @Min(1) @Max(5) Integer serie,
    @NotBlank String nome,
    @NotNull Turno turno,
    @NotNull Integer professorRegenteId,
    @NotNull Integer modeloGradeId,
    List<ProfessorEspecificoRequest> professoresEspecificos
) {
    public record ProfessorEspecificoRequest(
        @NotNull Integer disciplinaId,
        @NotNull Integer professorId
    ) {}
}
