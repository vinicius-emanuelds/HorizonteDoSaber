package com.poo.siga.model;

import com.poo.siga.model.enums.TipoAvaliacao;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "avaliacao")
public class Avaliacao {

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
    @Column(nullable = false)
    private Integer bimestre; // 1, 2, 3, 4

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAvaliacao tipoAvaliacao; // AV1, AV2, REC

    @NotNull
    @Column(nullable = false)
    private LocalDate dataExata;

    private String conteudo;
}
