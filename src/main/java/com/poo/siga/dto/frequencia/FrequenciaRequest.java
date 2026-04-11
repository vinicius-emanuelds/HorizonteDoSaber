package com.poo.siga.dto.frequencia;

import com.poo.siga.model.enums.StatusFrequencia;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record FrequenciaRequest(
    @NotNull Integer turmaId,
    @NotNull Integer disciplinaId,
    @NotNull Integer alunoId,
    @NotNull LocalDate data,
    @NotNull StatusFrequencia status,
    String justificativa
) {}
