package com.poo.siga.dto.nota;

import com.poo.siga.model.enums.TipoAvaliacao;
import jakarta.validation.constraints.*;

public record NotaRequest(
    @NotNull Integer turmaId,
    @NotNull Integer disciplinaId,
    @NotNull Integer alunoId,
    @NotNull @Min(1) @Max(4) Integer periodo,
    @NotNull TipoAvaliacao tipoAvaliacao,
    @NotNull @DecimalMin("0.0") @DecimalMax("10.0") Double valor
) {}
