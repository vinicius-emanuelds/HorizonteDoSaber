package com.poo.siga.dto.grade;

import com.poo.siga.dto.disciplina.DisciplinaResponse;
import com.poo.siga.model.ModeloGrade;
import com.poo.siga.model.ModeloGradeAula;

import java.time.DayOfWeek;
import java.util.List;
import java.util.stream.Collectors;

public record ModeloGradeResponse(
    Integer id,
    Integer anoLetivo,
    Integer serie,
    String nome,
    List<AulaResponse> aulas
) {
    public record AulaResponse(
        Integer id,
        DisciplinaResponse disciplina,
        DayOfWeek diaSemana,
        Integer numeroAula
    ) {
        public static AulaResponse from(ModeloGradeAula a) {
            return new AulaResponse(
                a.getId(),
                DisciplinaResponse.from(a.getDisciplina()),
                a.getDiaSemana(),
                a.getNumeroAula()
            );
        }
    }

    public static ModeloGradeResponse from(ModeloGrade m) {
        return new ModeloGradeResponse(
            m.getId(),
            m.getAnoLetivo(),
            m.getSerie(),
            m.getNome(),
            m.getAulas() != null ? m.getAulas().stream().map(AulaResponse::from).collect(Collectors.toList()) : List.of()
        );
    }
}
