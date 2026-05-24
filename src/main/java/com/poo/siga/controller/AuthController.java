package com.poo.siga.controller;

import com.poo.siga.dto.auth.*;
import com.poo.siga.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação")
public class AuthController {

    private final AuthService authService;

    @Value("${jwt.expiration:1800000}")
    private long jwtExpiration;

    @PostMapping("/login")
    @Operation(summary = "Realiza login e retorna JWT")
    public ResponseEntity<JwtResponse> login(
            @RequestBody @Valid LoginRequest req,
            HttpServletResponse response) {

        JwtResponse jwt = authService.login(req);

        // Cookie seguro para navegação via browser (complementa o token no corpo)
        Cookie jwtCookie = new Cookie("jwt", jwt.token());
        jwtCookie.setHttpOnly(true); // inacessível via JavaScript (proteção XSS)
        jwtCookie.setSecure(true); // apenas via HTTPS
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge((int) (jwtExpiration / 1000)); // converte ms → segundos
        jwtCookie.setAttribute("SameSite", "Strict"); // proteção CSRF

        response.addCookie(jwtCookie);

        return ResponseEntity.ok(jwt);
    }

    @PostMapping("/trocar-senha")
    @Operation(summary = "Trocar senha do usuário autenticado")
    public ResponseEntity<Void> trocarSenha(
            @RequestBody @Valid TrocarSenhaRequest req,
            Authentication auth) {
        authService.trocarSenha(auth.getName(), req.senhaAtual(), req.novaSenha());
        return ResponseEntity.noContent().build();
    }
}
