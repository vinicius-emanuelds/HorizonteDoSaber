package com.poo.siga.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.DayOfWeek;

@Data
@Entity
@Table(name = "grade_horaria", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"turma_id", "dia_semana", "numero_aula"})
})
public class GradeHoraria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "turma_id", nullable = false)
    private Turma turma;

    @ManyToOne(optional = false)
    @JoinColumn(name = "disciplina_id", nullable = false)
    private Disciplina disciplina;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false)
    private DayOfWeek diaSemana; // Ex: MONDAY, TUESDAY

    @NotNull
    @Min(1) @Max(5)
    @Column(name = "numero_aula", nullable = false)
    private Integer numeroAula; // 1 a 5
}
