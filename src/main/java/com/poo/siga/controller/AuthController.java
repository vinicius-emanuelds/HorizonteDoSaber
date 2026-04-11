package com.poo.siga.controller;

import com.poo.siga.dto.auth.*;
import com.poo.siga.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Realiza login e retorna JWT")
    public ResponseEntity<JwtResponse> login(@RequestBody @Valid LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/trocar-senha")
    @Operation(summary = "Trocar senha do usuário autenticado")
    public ResponseEntity<Void> trocarSenha(@RequestBody Map<String, String> body, Authentication auth) {
        authService.trocarSenha(auth.getName(), body.get("senhaAtual"), body.get("novaSenha"));
        return ResponseEntity.noContent().build();
    }
}
