package com.poo.siga.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "turma_disciplina_professor", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"turma_id", "disciplina_id"})
})
public class TurmaDisciplinaProfessor {

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
    @JoinColumn(name = "professor_id", nullable = false)
    private Professor professor;
}
