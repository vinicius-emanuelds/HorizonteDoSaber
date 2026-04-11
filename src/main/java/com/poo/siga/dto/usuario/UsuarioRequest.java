package com.poo.siga.dto.usuario;

import com.poo.siga.model.enums.Role;
import jakarta.validation.constraints.*;

public record UsuarioRequest(
    @NotBlank String nomeCompleto,
    @NotBlank @Email String email,
    @NotBlank String login,
    @NotBlank String senha,
    @NotNull Role role,
    Integer professorId
) {}
