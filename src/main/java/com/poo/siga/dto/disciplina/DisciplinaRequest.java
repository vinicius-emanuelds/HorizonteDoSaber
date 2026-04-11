package com.poo.siga.dto.disciplina;

import jakarta.validation.constraints.*;

public record DisciplinaRequest(
    @NotBlank String descricao,
    @NotNull @Min(1) Integer cargaHorariaAnual
) {}
