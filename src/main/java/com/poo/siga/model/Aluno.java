package com.poo.siga.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "aluno")
public class Aluno extends Pessoa {

    @Column(unique = true, nullable = false)
    private String ra;

    @NotBlank(message = "O nome do responsável legal é obrigatório")
    @Column(nullable = false)
    private String nomeResponsavel;

    @NotBlank(message = "O CPF do responsável é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "CPF do responsável deve conter 11 dígitos numéricos")
    @Column(nullable = false, length = 11)
    private String cpfResponsavel;
}