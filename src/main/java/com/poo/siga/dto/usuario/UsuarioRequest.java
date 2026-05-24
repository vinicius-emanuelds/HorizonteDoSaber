package com.poo.siga.dto.usuario;

import com.poo.siga.model.enums.Role;
import jakarta.validation.constraints.*;

/**
 * DTO de criação/atualização de usuário.
 * A senha é opcional na atualização (se omitida, a senha existente é mantida).
 * Quando fornecida, deve atender aos critérios mínimos de complexidade.
 */
public record UsuarioRequest(
    @NotBlank String nomeCompleto,
    @NotBlank @Email String email,
    @NotBlank String login,

    // Opcional na atualização; obrigatória na criação (validado no service).
    @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
        message = "A senha deve conter pelo menos uma letra maiúscula, uma minúscula e um número"
    )
    String senha,

    @NotNull Role role,
    Integer professorId
) {}
