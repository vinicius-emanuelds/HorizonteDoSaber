package com.poo.siga.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "professor")
public class Professor extends Pessoa {

    @Column(unique = true, nullable = false)
    private String codigoFuncional;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.poo.siga.model.enums.EspecialidadeProfessor especialidade = com.poo.siga.model.enums.EspecialidadeProfessor.REGENTE;
}