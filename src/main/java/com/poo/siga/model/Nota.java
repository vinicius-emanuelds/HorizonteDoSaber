package com.poo.siga.model;

import com.poo.siga.model.enums.TipoAvaliacao;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "nota", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"turma_id", "disciplina_id", "aluno_id", "periodo", "tipo_avaliacao"})
})
public class Nota {

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

    @NotNull(message = "O período (bimestre) é obrigatório")
    @Min(value = 1, message = "Período mínimo: 1º bimestre")
    @Max(value = 4, message = "Período máximo: 4º bimestre")
    @Column(nullable = false)
    private Integer periodo;

    @NotNull(message = "O tipo de avaliação é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_avaliacao", nullable = false)
    private TipoAvaliacao tipoAvaliacao;

    @NotNull(message = "A nota é obrigatória")
    @DecimalMin(value = "0.0", message = "A nota mínima é 0,0")
    @DecimalMax(value = "10.0", message = "A nota máxima é 10,0")
    @Column(nullable = false)
    private Double valor;

    @Column(nullable = false)
    private LocalDateTime dataLancamento;

    private String usuarioLancamento;

    @PrePersist
    public void prePersist() {
        if (this.dataLancamento == null) {
            this.dataLancamento = LocalDateTime.now();
        }
    }
}
