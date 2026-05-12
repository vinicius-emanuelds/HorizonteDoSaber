package com.poo.siga.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "modelo_grade", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"ano_letivo", "serie", "nome"})
})
public class ModeloGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "O ano letivo é obrigatório")
    @Column(name = "ano_letivo", nullable = false)
    private Integer anoLetivo;

    @NotNull(message = "A série é obrigatória")
    @Min(value = 1, message = "Série mínima: 1º ano")
    @Max(value = 5, message = "Série máxima: 5º ano")
    @Column(nullable = false)
    private Integer serie;

    @NotBlank(message = "O nome do modelo é obrigatório")
    @Column(nullable = false)
    private String nome;

    @OneToMany(mappedBy = "modeloGrade", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ModeloGradeAula> aulas;
}
