package com.poo.siga.model;

import com.poo.siga.model.enums.StatusFrequencia;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@Data
@Entity
@Table(name = "frequencia", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"turma_id", "disciplina_id", "aluno_id", "data", "numero_aula"})
})
public class Frequencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "turma_id", nullable = false)
    private Turma turma;

    @ManyToOne(optional = false)
    @JoinColumn(name = "disciplina_id", nullable = false)
    private Disciplina disciplina;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @NotNull(message = "A data é obrigatória")
    @Column(nullable = false)
    private LocalDate data;

    /**
     * Número de ordem da aula no dia (1 a 5).
     * Frequência é registrada por aula, não por dia letivo.
     * Cada turno tem 5 aulas de 45 min (intervalo após a 3ª aula).
     */
    @NotNull(message = "O número da aula é obrigatório")
    @Min(value = 1, message = "O número da aula mínimo é 1")
    @Max(value = 5, message = "O número da aula máximo é 5")
    @Column(name = "numero_aula", nullable = false)
    private Integer numeroAula;

    @NotNull(message = "O status é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusFrequencia status;

    private String justificativa;
}
