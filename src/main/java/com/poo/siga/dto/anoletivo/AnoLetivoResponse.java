package com.poo.siga.dto.anoletivo;

import com.poo.siga.model.AnoLetivo;
import com.poo.siga.model.SemanaAvaliacao;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public record AnoLetivoResponse(
    Integer id,
    Integer ano,
    LocalDate dataInicio,
    LocalDate dataEncerramento,
    Integer diasLetivos,
    boolean encerrado,
    List<LocalDate> feriados,
    List<SemanaAvaliacaoResponse> semanasAvaliacao
) {
    public record SemanaAvaliacaoResponse(
        Integer bimestre,
        String tipo,
        LocalDate dataInicio,
        LocalDate dataFim
    ) {
        public static SemanaAvaliacaoResponse from(SemanaAvaliacao s) {
            return new SemanaAvaliacaoResponse(s.getBimestre(), s.getTipo(), s.getDataInicio(), s.getDataFim());
        }
    }
    public static AnoLetivoResponse from(AnoLetivo a) {
        return new AnoLetivoResponse(
            a.getId(),
            a.getAno(),
            a.getDataInicio(),
            a.getDataEncerramento(),
            a.getDiasLetivos(),
            a.isEncerrado(),
            a.getFeriados() != null ? new java.util.ArrayList<>(a.getFeriados()) : List.of(),
            a.getSemanasAvaliacao() != null ? 
                a.getSemanasAvaliacao().stream().map(SemanaAvaliacaoResponse::from).collect(Collectors.toList()) : 
                List.of()
        );
    }
}
