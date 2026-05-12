package com.poo.siga.controller;

import com.poo.siga.dto.anoletivo.AnoLetivoRequest;
import com.poo.siga.dto.anoletivo.AnoLetivoResponse;
import com.poo.siga.service.AnoLetivoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/anos-letivos")
@RequiredArgsConstructor
@Tag(name = "Anos Letivos")
@SecurityRequirement(name = "bearerAuth")
public class AnoLetivoController {

    private final AnoLetivoService service;

    /** Lista todos os anos letivos (qualquer usuário autenticado) */
    @GetMapping
    public ResponseEntity<List<AnoLetivoResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    /** Retorna o ano letivo ativo atual (qualquer usuário autenticado) */
    @GetMapping("/ativo")
    public ResponseEntity<AnoLetivoResponse> buscarAtivo() {
        return ResponseEntity.ok(service.buscarAtivo());
    }

    /** Busca por ID (qualquer usuário autenticado) */
    @GetMapping("/{id}")
    public ResponseEntity<AnoLetivoResponse> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    /** Cria novo ano letivo — ADMIN only (via SecurityConfig) */
    @PostMapping
    public ResponseEntity<AnoLetivoResponse> criar(@RequestBody @Valid AnoLetivoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(req));
    }

    /** Atualiza ano letivo — ADMIN only (via SecurityConfig) */
    @PutMapping("/{id}")
    public ResponseEntity<AnoLetivoResponse> atualizar(@PathVariable Integer id,
                                                       @RequestBody @Valid AnoLetivoRequest req) {
        return ResponseEntity.ok(service.atualizar(id, req));
    }

    /**
     * Encerra o ano letivo — ADMIN only.
     * Após encerrado, bloqueia novos lançamentos de nota e frequência para
     * todas as turmas daquele ano.
     */
    @PatchMapping("/{id}/encerrar")
    public ResponseEntity<AnoLetivoResponse> encerrar(@PathVariable Integer id) {
        return ResponseEntity.ok(service.encerrar(id));
    }
}
