package com.poo.siga.dto.auth;

public record JwtResponse(String token, String role, String login, String nome, boolean primeiroAcesso) {}
