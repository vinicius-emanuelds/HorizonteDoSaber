package com.poo.siga.dto.grade;

import com.poo.siga.model.GradeHoraria;

import java.time.DayOfWeek;

/**
 * DTO seguro para serialização da GradeHoraria, evitando recursão JSON
 */
public record GradeHorariaResponse(
    Integer id,
    DayOfWeek diaSemana,
    Integer numeroAula,
    Integer disciplinaId,
    String disciplinaDescricao
) {
    public static GradeHorariaResponse from(GradeHoraria g) {
        return new GradeHorariaResponse(
            g.getId(),
            g.getDiaSemana(),
            g.getNumeroAula(),
            g.getDisciplina().getId(),
            g.getDisciplina().getDescricao()
        );
    }
}
