package com.poo.siga.controller;

import com.poo.siga.dto.nota.*;
import com.poo.siga.service.NotaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notas")
@RequiredArgsConstructor
@Tag(name = "Notas")
@SecurityRequirement(name = "bearerAuth")
public class NotaController {

    private final NotaService service;

    @GetMapping("/turma/{turmaId}")
    public ResponseEntity<List<NotaResponse>> listarPorTurma(@PathVariable Integer turmaId) {
        return ResponseEntity.ok(service.listarPorTurma(turmaId));
    }

    @GetMapping("/turma/{turmaId}/disciplina/{disciplinaId}/periodo/{periodo}")
    public ResponseEntity<List<NotaResponse>> listarPorTurmaDisciplinaPeriodo(
            @PathVariable Integer turmaId, @PathVariable Integer disciplinaId,
            @PathVariable Integer periodo) {
        return ResponseEntity.ok(service.listarPorTurmaEDisciplina(turmaId, disciplinaId, periodo));
    }

    @GetMapping("/aluno/{alunoId}")
    public ResponseEntity<List<NotaResponse>> listarPorAluno(@PathVariable Integer alunoId) {
        return ResponseEntity.ok(service.listarPorAluno(alunoId));
    }

    @PostMapping
    public ResponseEntity<NotaResponse> lancar(@RequestBody @Valid NotaRequest req, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.lancar(req, auth != null ? auth.getName() : "sistema"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NotaResponse> atualizar(@PathVariable Integer id,
                                                   @RequestBody Map<String, Double> body,
                                                   Authentication auth) {
        return ResponseEntity.ok(service.atualizar(id, body.get("valor"),
                auth != null ? auth.getName() : "sistema"));
    }

    @GetMapping("/media-bimestral")
    public ResponseEntity<Map<String, Double>> mediaBimestral(
            @RequestParam Integer turmaId, @RequestParam Integer disciplinaId,
            @RequestParam Integer alunoId, @RequestParam Integer periodo) {
        Double media = service.calcularMediaBimestral(turmaId, disciplinaId, alunoId, periodo);
        return ResponseEntity.ok(Map.of("media", media != null ? media : 0.0));
    }

    @GetMapping("/media-anual")
    public ResponseEntity<Map<String, Double>> mediaAnual(
            @RequestParam Integer turmaId, @RequestParam Integer disciplinaId,
            @RequestParam Integer alunoId) {
        Double media = service.calcularMediaAnual(turmaId, disciplinaId, alunoId);
        return ResponseEntity.ok(Map.of("media", media != null ? media : 0.0));
    }
}
