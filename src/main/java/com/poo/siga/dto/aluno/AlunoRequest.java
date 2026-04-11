package com.poo.siga.dto.aluno;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record AlunoRequest(
    @NotBlank @Size(min = 3, max = 100) String nome,
    @NotNull LocalDate dataNascimento,
    @NotBlank @Pattern(regexp = "\\d{11}") String cpf,
    @NotBlank @Email String email,
    @NotBlank String nomeResponsavel,
    @NotBlank @Pattern(regexp = "\\d{11}") String cpfResponsavel
) {}
