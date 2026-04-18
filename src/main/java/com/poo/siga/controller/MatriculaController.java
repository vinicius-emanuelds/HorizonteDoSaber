package com.poo.siga.controller;

import com.poo.siga.dto.matricula.*;
import com.poo.siga.model.enums.SituacaoMatricula;
import com.poo.siga.service.MatriculaService;
import io.swagger.v3.oas.annotations.Operation;
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
import java.util.Map;

@RestController
@RequestMapping("/api/matriculas")
@RequiredArgsConstructor
@Tag(name = "Matrículas")
@SecurityRequirement(name = "bearerAuth")
public class MatriculaController {

    private final MatriculaService service;

    @GetMapping
    public ResponseEntity<Page<MatriculaResponse>> listar(
            @RequestParam(required = false) Integer alunoId,
            @RequestParam(required = false) Integer turmaId,
            @RequestParam(required = false) Integer anoLetivo,
            @RequestParam(required = false) SituacaoMatricula situacao,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(service.listar(alunoId, turmaId, anoLetivo, situacao, pageable));
    }

    @GetMapping("/turma/{turmaId}")
    public ResponseEntity<List<MatriculaResponse>> listarPorTurma(@PathVariable Integer turmaId) {
        return ResponseEntity.ok(service.listarPorTurma(turmaId));
    }

    @GetMapping("/aluno/{alunoId}")
    @Operation(summary = "Histórico de matrículas do aluno")
    public ResponseEntity<List<MatriculaResponse>> listarPorAluno(@PathVariable Integer alunoId) {
        return ResponseEntity.ok(service.listarPorAluno(alunoId));
    }

    @PostMapping
    public ResponseEntity<MatriculaResponse> criar(@RequestBody @Valid MatriculaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(req));
    }

    @PatchMapping("/{id}/trancar")
    public ResponseEntity<MatriculaResponse> trancar(@PathVariable Integer id,
                                                      @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(service.trancar(id, body.getOrDefault("motivo", "")));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<MatriculaResponse> cancelar(@PathVariable Integer id,
                                                       @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(service.cancelar(id, body.getOrDefault("motivo", "")));
    }

    @PatchMapping("/{id}/concluir")
    public ResponseEntity<MatriculaResponse> concluir(@PathVariable Integer id,
                                                       @RequestBody(required = false) Map<String, String> body) {
        return ResponseEntity.ok(service.concluir(id));
    }
}
