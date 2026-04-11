package com.poo.siga.controller;

import com.poo.siga.dto.frequencia.*;
import com.poo.siga.service.FrequenciaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/frequencias")
@RequiredArgsConstructor
@Tag(name = "Frequência")
@SecurityRequirement(name = "bearerAuth")
public class FrequenciaController {

    private final FrequenciaService service;

    @GetMapping("/turma/{turmaId}/disciplina/{disciplinaId}")
    public ResponseEntity<List<FrequenciaResponse>> listarPorTurmaEData(
            @PathVariable Integer turmaId, @PathVariable Integer disciplinaId,
            @RequestParam String data) {
        return ResponseEntity.ok(service.listarPorTurmaEData(turmaId, disciplinaId, data));
    }

    @GetMapping("/aluno/{alunoId}")
    public ResponseEntity<List<FrequenciaResponse>> listarPorAluno(@PathVariable Integer alunoId) {
        return ResponseEntity.ok(service.listarPorAluno(alunoId));
    }

    @PostMapping
    public ResponseEntity<FrequenciaResponse> registrar(@RequestBody @Valid FrequenciaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FrequenciaResponse> atualizar(@PathVariable Integer id,
                                                         @RequestBody @Valid FrequenciaRequest req) {
        return ResponseEntity.ok(service.atualizar(id, req));
    }

    @GetMapping("/percentual")
    public ResponseEntity<Map<String, Double>> percentual(
            @RequestParam Integer alunoId, @RequestParam Integer disciplinaId) {
        Double pct = service.calcularPercentualPresenca(alunoId, disciplinaId);
        return ResponseEntity.ok(Map.of("percentual", pct != null ? pct : 0.0));
    }
}
