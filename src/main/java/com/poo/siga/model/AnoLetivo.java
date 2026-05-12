package com.poo.siga.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    /** Quantidade de dias letivos planejados (informativo) */
    private Integer diasLetivos;

    /** Indica se o ano letivo foi encerrado (bloqueia lançamentos) */
    @Column(nullable = false)
    private boolean encerrado = false;

    /**
     * Feriados do ano letivo (datas em que não há aulas).
     * Armazenados em tabela separada ano_letivo_feriados.
     */
    @ElementCollection
    @CollectionTable(name = "ano_letivo_feriados",
            joinColumns = @JoinColumn(name = "ano_letivo_id"))
    @Column(name = "data_feriado")
    @OrderBy("data_feriado ASC")
    private List<LocalDate> feriados = new ArrayList<>();

    /**
     * Semanas de avaliação do ano letivo (informativo).
     * O coordenador define a semana, e o professor marca a data exata.
     */
    @ElementCollection
    @CollectionTable(name = "ano_letivo_semanas_avaliacao",
            joinColumns = @JoinColumn(name = "ano_letivo_id"))
    private List<SemanaAvaliacao> semanasAvaliacao = new ArrayList<>();
}
