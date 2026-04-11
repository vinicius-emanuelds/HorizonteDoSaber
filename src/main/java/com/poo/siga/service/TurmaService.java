package com.poo.siga.service;

import com.poo.siga.dto.turma.*;
import com.poo.siga.model.Turma;
import com.poo.siga.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TurmaService {

    private final TurmaRepository turmaRepository;
    private final ProfessorRepository professorRepository;
    private final MatriculaRepository matriculaRepository;

    @Transactional(readOnly = true)
    public Page<TurmaResponse> listar(Integer anoLetivo, Integer serie, Boolean ativo, Pageable pageable) {
        return turmaRepository.buscarComFiltros(anoLetivo, serie, ativo, pageable).map(TurmaResponse::from);
    }

    @Transactional(readOnly = true)
    public List<TurmaResponse> listarPorAnoLetivo(Integer anoLetivo) {
        return turmaRepository.findByAnoLetivoAndAtivo(anoLetivo, true)
                .stream().map(TurmaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TurmaResponse buscarPorId(Integer id) {
        return TurmaResponse.from(findOrThrow(id));
    }

    @Transactional
    public TurmaResponse criar(TurmaRequest req) {
        var professor = professorRepository.findById(req.professorRegenteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Professor regente não encontrado"));
        var turma = new Turma();
        turma.setAnoLetivo(req.anoLetivo());
        turma.setSerie(req.serie());
        turma.setNome(req.nome());
        turma.setTurno(req.turno());
        turma.setProfessorRegente(professor);
        return TurmaResponse.from(turmaRepository.save(turma));
    }

    @Transactional
    public TurmaResponse atualizar(Integer id, TurmaRequest req) {
        var turma = findOrThrow(id);
        var professor = professorRepository.findById(req.professorRegenteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Professor regente não encontrado"));
        turma.setAnoLetivo(req.anoLetivo());
        turma.setSerie(req.serie());
        turma.setNome(req.nome());
        turma.setTurno(req.turno());
        turma.setProfessorRegente(professor);
        return TurmaResponse.from(turmaRepository.save(turma));
    }

    @Transactional
    public void inativar(Integer id) {
        var turma = findOrThrow(id);
        turma.setAtivo(false);
        turmaRepository.save(turma);
    }

    @Transactional
    public void excluir(Integer id) {
        var turma = findOrThrow(id);
        if (matriculaRepository.countByTurmaId(id) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Não é possível excluir turma com alunos vinculados.");
        }
        turmaRepository.delete(turma);
    }

    private Turma findOrThrow(Integer id) {
        return turmaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma não encontrada"));
    }
}
