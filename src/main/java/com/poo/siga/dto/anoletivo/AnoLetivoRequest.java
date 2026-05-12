package com.poo.siga.dto.anoletivo;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record AnoLetivoRequest(
    @NotNull(message = "O ano é obrigatório") Integer ano,
    @NotNull(message = "A data de início é obrigatória") LocalDate dataInicio,
    @NotNull(message = "A data de encerramento é obrigatória") LocalDate dataEncerramento,
    Integer diasLetivos,
    List<LocalDate> feriados,
    List<SemanaAvaliacaoRequest> semanasAvaliacao
) {
    public record SemanaAvaliacaoRequest(
        Integer bimestre,
        String tipo,
        LocalDate dataInicio,
        LocalDate dataFim
    ) {}
}
