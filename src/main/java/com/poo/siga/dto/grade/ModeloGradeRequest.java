package com.poo.siga.dto.grade;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.util.List;

public record ModeloGradeRequest(
    @NotNull(message = "O ano letivo é obrigatório") Integer anoLetivo,
    @NotNull(message = "A série é obrigatória") @Min(1) @Max(5) Integer serie,
    @NotBlank(message = "O nome é obrigatório") String nome,
    List<AulaRequest> aulas
) {
    public record AulaRequest(
        @NotNull Integer disciplinaId,
        @NotNull DayOfWeek diaSemana,
        @NotNull @Min(1) @Max(5) Integer numeroAula
    ) {}
}
