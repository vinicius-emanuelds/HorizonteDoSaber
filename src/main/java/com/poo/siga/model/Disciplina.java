package com.poo.siga.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
@Table(name = "disciplina")
public class Disciplina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String codigo;

    @NotBlank(message = "A descrição da disciplina é obrigatória")
    @Column(nullable = false)
    private String descricao;

    @NotNull(message = "A carga horária anual é obrigatória")
    @Min(value = 1, message = "A carga horária deve ser no mínimo 1 hora-aula")
    @Column(nullable = false)
    private Integer cargaHorariaAnual;

    @Column(nullable = false)
    private boolean ativo = true;
}