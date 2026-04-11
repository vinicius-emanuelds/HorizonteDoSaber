package com.poo.siga.controller;

import com.poo.siga.dto.disciplina.*;
import com.poo.siga.service.DisciplinaService;
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

@RestController
@RequestMapping("/api/disciplinas")
@RequiredArgsConstructor
@Tag(name = "Disciplinas")
@SecurityRequirement(name = "bearerAuth")
public class DisciplinaController {

    private final DisciplinaService service;

    @GetMapping
    public ResponseEntity<Page<DisciplinaResponse>> listar(
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) Boolean ativo,
            @PageableDefault(size = 20, sort = "descricao") Pageable pageable) {
        return ResponseEntity.ok(service.listar(descricao, codigo, ativo, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DisciplinaResponse> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<DisciplinaResponse> criar(@RequestBody @Valid DisciplinaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DisciplinaResponse> atualizar(@PathVariable Integer id,
                                                         @RequestBody @Valid DisciplinaRequest req) {
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
