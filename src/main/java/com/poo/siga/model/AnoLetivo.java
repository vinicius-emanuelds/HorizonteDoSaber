package com.poo.siga.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "ano_letivo")
public class AnoLetivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "O ano é obrigatório")
    @Column(unique = true, nullable = false)
    private Integer ano;

    @NotNull(message = "A data de início é obrigatória")
    @Column(nullable = false)
    private LocalDate dataInicio;

    @NotNull(message = "A data de encerramento é obrigatória")
    @Column(nullable = false)
    private LocalDate dataEncerramento;

    @Column(nullable = false)
    private boolean encerrado = false;
}
