package com.poo.siga.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para troca de senha pelo próprio usuário autenticado.
 * Exige senha atual (para confirmar identidade) e nova senha com critérios mínimos de complexidade.
 */
public record TrocarSenhaRequest(

    @NotBlank(message = "A senha atual é obrigatória")
    String senhaAtual,

    @NotBlank(message = "A nova senha é obrigatória")
    @Size(min = 8, message = "A nova senha deve ter no mínimo 8 caracteres")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
        message = "A nova senha deve conter pelo menos uma letra maiúscula, uma minúscula e um número"
    )
    String novaSenha

) {}
