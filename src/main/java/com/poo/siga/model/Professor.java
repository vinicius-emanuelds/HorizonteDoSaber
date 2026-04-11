package com.poo.siga.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.concurrent.atomic.AtomicInteger;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "professor")
public class Professor extends Pessoa {

    private static final AtomicInteger SEQ = new AtomicInteger(1);

    @Column(unique = true, nullable = false)
    private String codigoFuncional;

    @PrePersist
    public void gerarCodigoFuncional() {
        if (this.codigoFuncional == null) {
            this.codigoFuncional = "RF" + String.format("%06d", SEQ.getAndIncrement());
        }
    }
}