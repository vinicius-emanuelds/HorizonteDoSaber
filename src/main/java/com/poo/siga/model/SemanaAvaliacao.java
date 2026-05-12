package com.poo.siga.model;

import jakarta.persistence.Embeddable;
import lombok.Data;
import java.time.LocalDate;

@Data
@Embeddable
public class SemanaAvaliacao {

    private Integer bimestre; // 1, 2, 3, 4
    
    private String tipo; // "AV1", "AV2", "REC"
    
    private LocalDate dataInicio;
    
    private LocalDate dataFim;
}
