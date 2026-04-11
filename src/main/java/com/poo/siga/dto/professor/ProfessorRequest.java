package com.poo.siga.dto.professor;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record ProfessorRequest(
    @NotBlank @Size(min = 3, max = 100) String nome,
    @NotNull LocalDate dataNascimento,
    @NotBlank @Pattern(regexp = "\\d{11}") String cpf,
    @NotBlank @Email String email
) {}
