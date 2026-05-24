package com.poo.siga.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para redefinição administrativa de senha (usada por ADMIN).
 * A nova senha deve atender aos critérios mínimos de complexidade.
 */
public record RedefinirSenhaRequest(

    @NotBlank(message = "A nova senha é obrigatória")
    @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
        message = "A senha deve conter pelo menos uma letra maiúscula, uma minúscula e um número"
    )
    String novaSenha

) {}
