package com.poo.siga.model;

import com.poo.siga.model.enums.Turno;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@Entity
@Table(name = "turma", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"ano_letivo", "serie", "nome", "turno"})
})
public class Turma {

    private static final AtomicInteger SEQ = new AtomicInteger(1);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String codigo;

    @NotNull(message = "O ano letivo é obrigatório")
    @Column(name = "ano_letivo", nullable = false)
    private Integer anoLetivo;

    @NotNull(message = "A série é obrigatória")
    @Min(value = 1, message = "Série mínima: 1º ano")
    @Max(value = 5, message = "Série máxima: 5º ano")
    @Column(nullable = false)
    private Integer serie;

    @NotBlank(message = "O nome/identificação da turma é obrigatório")
    @Column(nullable = false)
    private String nome;

    @NotNull(message = "O turno é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Turno turno;

    @ManyToOne
    @JoinColumn(name = "professor_regente_id")
    private Professor professorRegente;

    @Column(nullable = false)
    private boolean ativo = true;

    @OneToMany(mappedBy = "turma", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TurmaDisciplinaProfessor> disciplinasProfessores;

    @PrePersist
    public void gerarCodigo() {
        if (this.codigo == null) {
            this.codigo = "TUR" + String.format("%05d", SEQ.getAndIncrement());
        }
    }

    public String getIdentificacao() {
        return serie + "º " + nome + " - " + turno.name();
    }
}