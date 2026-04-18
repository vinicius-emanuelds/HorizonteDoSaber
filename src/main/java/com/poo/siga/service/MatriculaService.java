package com.poo.siga.service;

import com.poo.siga.dto.matricula.*;
import com.poo.siga.model.Matricula;
import com.poo.siga.model.enums.SituacaoMatricula;
import com.poo.siga.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatriculaService {

    private final MatriculaRepository matriculaRepository;
    private final AlunoRepository alunoRepository;
    private final TurmaRepository turmaRepository;

    @Transactional(readOnly = true)
    public Page<MatriculaResponse> listar(Integer alunoId, Integer turmaId, Integer anoLetivo,
                                           SituacaoMatricula situacao, Pageable pageable) {
        return matriculaRepository.buscarComFiltros(alunoId, turmaId, anoLetivo, situacao, pageable)
                .map(MatriculaResponse::from);
    }

    @Transactional(readOnly = true)
    public List<MatriculaResponse> listarPorTurma(Integer turmaId) {
        return matriculaRepository.findByTurmaId(turmaId).stream().map(MatriculaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<MatriculaResponse> listarPorAluno(Integer alunoId) {
        return matriculaRepository.findByAlunoId(alunoId).stream().map(MatriculaResponse::from).toList();
    }

    @Transactional
    public MatriculaResponse criar(MatriculaRequest req) {
        var aluno = alunoRepository.findById(req.alunoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado"));
        var turma = turmaRepository.findById(req.turmaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma não encontrada"));

        if (!aluno.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Aluno inativo não pode ser matriculado");
        }
        if (matriculaRepository.existsByAlunoIdAndAnoLetivoAndSituacao(
                aluno.getId(), turma.getAnoLetivo(), SituacaoMatricula.ATIVA)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Aluno já possui matrícula ativa neste ano letivo");
        }

        var m = new Matricula();
        m.setAluno(aluno);
        m.setTurma(turma);
        m.setAnoLetivo(turma.getAnoLetivo());
        m.setSerie(turma.getSerie());
        m.setTurno(turma.getTurno());
        m.setDataMatricula(LocalDate.now());
        m.setSituacao(SituacaoMatricula.ATIVA);
        return MatriculaResponse.from(matriculaRepository.save(m));
    }

    @Transactional
    public MatriculaResponse trancar(Integer id, String motivo) {
        var m = findOrThrow(id);
        if (m.getSituacao() != SituacaoMatricula.ATIVA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Somente matrículas ativas podem ser trancadas");
        }
        m.setSituacao(SituacaoMatricula.TRANCADA);
        m.setMotivoCancelamento(motivo);
        m.setDataCancelamento(LocalDate.now());
        return MatriculaResponse.from(matriculaRepository.save(m));
    }

    @Transactional
    public MatriculaResponse cancelar(Integer id, String motivo) {
        var m = findOrThrow(id);
        if (m.getSituacao() == SituacaoMatricula.CANCELADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Matrícula já cancelada");
        }
        m.setSituacao(SituacaoMatricula.CANCELADA);
        m.setMotivoCancelamento(motivo);
        m.setDataCancelamento(LocalDate.now());
        return MatriculaResponse.from(matriculaRepository.save(m));
    }

    @Transactional
    public MatriculaResponse concluir(Integer id) {
        var m = findOrThrow(id);
        if (m.getSituacao() != SituacaoMatricula.ATIVA && m.getSituacao() != SituacaoMatricula.TRANCADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Somente matrículas ativas ou trancadas podem ser concluídas");
        }
        m.setSituacao(SituacaoMatricula.CONCLUIDA);
        m.setDataCancelamento(LocalDate.now());
        return MatriculaResponse.from(matriculaRepository.save(m));
    }

    private Matricula findOrThrow(Integer id) {
        return matriculaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Matrícula não encontrada"));
    }
}
