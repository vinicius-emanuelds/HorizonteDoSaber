package com.poo.siga.controller;

import com.poo.siga.dto.grade.ModeloGradeRequest;
import com.poo.siga.dto.grade.ModeloGradeResponse;
import com.poo.siga.service.ModeloGradeService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modelos-grade")
@RequiredArgsConstructor
@Tag(name = "Modelos de Grade")
@SecurityRequirement(name = "bearerAuth")
public class ModeloGradeController {

    private final ModeloGradeService service;

    @GetMapping
    public ResponseEntity<List<ModeloGradeResponse>> listar(@RequestParam(required = false) Integer anoLetivo) {
        if (anoLetivo != null) {
            return ResponseEntity.ok(service.listarPorAno(anoLetivo));
        }
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModeloGradeResponse> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ModeloGradeResponse> criar(@RequestBody @Valid ModeloGradeRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModeloGradeResponse> atualizar(@PathVariable Integer id, @RequestBody @Valid ModeloGradeRequest req) {
        return ResponseEntity.ok(service.atualizar(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
