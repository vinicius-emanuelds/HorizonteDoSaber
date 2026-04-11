package com.poo.siga.controller;

import com.poo.siga.dto.turma.*;
import com.poo.siga.service.TurmaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/turmas")
@RequiredArgsConstructor
@Tag(name = "Turmas")
@SecurityRequirement(name = "bearerAuth")
public class TurmaController {

    private final TurmaService service;

    @GetMapping
    public ResponseEntity<Page<TurmaResponse>> listar(
            @RequestParam(required = false) Integer anoLetivo,
            @RequestParam(required = false) Integer serie,
            @RequestParam(required = false) Boolean ativo,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(service.listar(anoLetivo, serie, ativo, pageable));
    }

    @GetMapping("/ano/{anoLetivo}")
    public ResponseEntity<List<TurmaResponse>> listarPorAnoLetivo(@PathVariable Integer anoLetivo) {
        return ResponseEntity.ok(service.listarPorAnoLetivo(anoLetivo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TurmaResponse> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<TurmaResponse> criar(@RequestBody @Valid TurmaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TurmaResponse> atualizar(@PathVariable Integer id,
                                                    @RequestBody @Valid TurmaRequest req) {
        return ResponseEntity.ok(service.atualizar(id, req));
    }

    @PatchMapping("/{id}/inativar")
    public ResponseEntity<Void> inativar(@PathVariable Integer id) {
        service.inativar(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
