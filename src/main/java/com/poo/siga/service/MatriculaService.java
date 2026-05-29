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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatriculaService {

    private final MatriculaRepository matriculaRepository;
    private final AlunoRepository alunoRepository;
    private final TurmaRepository turmaRepository;

    @Transactional(readOnly = true)
    public Page<MatriculaResponse> listar(String nomeAluno, Integer turmaId, Integer anoLetivo,
                                           SituacaoMatricula situacao, Pageable pageable) {
        return matriculaRepository.buscarComFiltros(nomeAluno, turmaId, anoLetivo, situacao, pageable)
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

        var matricula = new Matricula();
        matricula.setNumero(gerarNumero(turma.getAnoLetivo()));
        matricula.setAluno(aluno);
        matricula.setTurma(turma);
        matricula.setAnoLetivo(turma.getAnoLetivo());
        matricula.setSerie(turma.getSerie());
        matricula.setTurno(turma.getTurno());
        matricula.setDataMatricula(LocalDate.now());
        matricula.setSituacao(SituacaoMatricula.ATIVA);
        return MatriculaResponse.from(matriculaRepository.save(matricula));
        // var m = new Matricula();
        // m.setNumero(gerarNumero(turma.getAnoLetivo()));
        // m.setAluno(aluno);
        // m.setTurma(turma);
        // m.setAnoLetivo(turma.getAnoLetivo());
        // m.setSerie(turma.getSerie());
        // m.setTurno(turma.getTurno());
        // m.setDataMatricula(LocalDate.now());
        // m.setSituacao(SituacaoMatricula.ATIVA);
        // return MatriculaResponse.from(matriculaRepository.save(m));
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
        var matricula = findOrThrow(id);
        if (matricula.getSituacao() == SituacaoMatricula.CANCELADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Matrícula já cancelada");
        }
        matricula.setSituacao(SituacaoMatricula.CANCELADA);
        matricula.setMotivoCancelamento(motivo);
        matricula.setDataCancelamento(LocalDate.now());
        return MatriculaResponse.from(matriculaRepository.save(matricula));
        // var m = findOrThrow(id);
        // if (m.getSituacao() == SituacaoMatricula.CANCELADA) {
        //     throw new ResponseStatusException(HttpStatus.CONFLICT, "Matrícula já cancelada");
        // }
        // m.setSituacao(SituacaoMatricula.CANCELADA);
        // m.setMotivoCancelamento(motivo);
        // m.setDataCancelamento(LocalDate.now());
        // return MatriculaResponse.from(matriculaRepository.save(m));
    }

    @Transactional
    public MatriculaResponse concluir(Integer id) {
        var matricula = findOrThrow(id);
        if (matricula.getSituacao() != SituacaoMatricula.ATIVA && matricula.getSituacao() != SituacaoMatricula.TRANCADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Somente matrículas ativas ou trancadas podem ser concluídas");
        }
        matricula.setSituacao(SituacaoMatricula.CONCLUIDA);
        matricula.setDataCancelamento(LocalDate.now());
        return MatriculaResponse.from(matriculaRepository.save(matricula));
        // var m = findOrThrow(id);
        // if (m.getSituacao() != SituacaoMatricula.ATIVA && m.getSituacao() != SituacaoMatricula.TRANCADA) {
        //     throw new ResponseStatusException(HttpStatus.CONFLICT,
        //         "Somente matrículas ativas ou trancadas podem ser concluídas");
        // }
        // m.setSituacao(SituacaoMatricula.CONCLUIDA);
        // m.setDataCancelamento(LocalDate.now());
        // return MatriculaResponse.from(matriculaRepository.save(m));
    }

    @Transactional
    public MatriculaResponse reativar(Integer id) {
        var matricula = findOrThrow(id);
        if (matricula.getSituacao() != SituacaoMatricula.CONCLUIDA && matricula.getSituacao() != SituacaoMatricula.TRANCADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Somente matrículas concluídas ou trancadas podem ser reativadas");
        }
        // Garante que o aluno não possui outra matrícula ATIVA no mesmo ano
        if (matriculaRepository.existsByAlunoIdAndAnoLetivoAndSituacao(
                matricula.getAluno().getId(), matricula.getAnoLetivo(), SituacaoMatricula.ATIVA)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Aluno já possui outra matrícula ativa neste ano letivo");
        }
        matricula.setSituacao(SituacaoMatricula.ATIVA);
        matricula.setMotivoCancelamento(null);
        matricula.setDataCancelamento(null);
        return MatriculaResponse.from(matriculaRepository.save(matricula));
        // var m = findOrThrow(id);
        // if (m.getSituacao() != SituacaoMatricula.CONCLUIDA && m.getSituacao() != SituacaoMatricula.TRANCADA) {
        //     throw new ResponseStatusException(HttpStatus.CONFLICT,
        //         "Somente matrículas concluídas ou trancadas podem ser reativadas");
        // }
        // // Garante que o aluno não possui outra matrícula ATIVA no mesmo ano
        // if (matriculaRepository.existsByAlunoIdAndAnoLetivoAndSituacao(
        //         m.getAluno().getId(), m.getAnoLetivo(), SituacaoMatricula.ATIVA)) {
        //     throw new ResponseStatusException(HttpStatus.CONFLICT,
        //         "Aluno já possui outra matrícula ativa neste ano letivo");
        // }
        // m.setSituacao(SituacaoMatricula.ATIVA);
        // m.setMotivoCancelamento(null);
        // m.setDataCancelamento(null);
        // return MatriculaResponse.from(matriculaRepository.save(m));
    }

    @Transactional
    public RematriculaResult rematricular(RematriculaRequest req) {
        var turmaOrigem = turmaRepository.findById(req.turmaOrigemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma de origem não encontrada"));
        var turmaDestino = turmaRepository.findById(req.turmaDestinoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma de destino não encontrada"));

        if (turmaOrigem.getId().equals(turmaDestino.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "A turma de origem e destino não podem ser iguais");
        }

        // Busca alunos da turma origem (ATIVA ou CONCLUIDA)
        var matriculasOrigem = matriculaRepository.findByTurmaId(turmaOrigem.getId())
                .stream()
                .filter(m -> m.getSituacao() == SituacaoMatricula.ATIVA ||
                             m.getSituacao() == SituacaoMatricula.CONCLUIDA)
                .toList();

        int total = matriculasOrigem.size();
        int matriculados = 0;
        List<String> ignorados = new ArrayList<>();

        for (var origem : matriculasOrigem) {
            var aluno = origem.getAluno();
            // Pula se já tem matrícula ATIVA no ano destino
            if (matriculaRepository.existsByAlunoIdAndAnoLetivoAndSituacao(
                    aluno.getId(), turmaDestino.getAnoLetivo(), SituacaoMatricula.ATIVA)) {
                ignorados.add(aluno.getNome() + " (" + aluno.getRa() + ")");
                continue;
            }
            if (!aluno.isAtivo()) {
                ignorados.add(aluno.getNome() + " (inativo)");
                continue;
            }
            var novaMatricula = new Matricula();
            novaMatricula.setNumero(gerarNumero(turmaDestino.getAnoLetivo()));
            novaMatricula.setAluno(aluno);
            novaMatricula.setTurma(turmaDestino);
            novaMatricula.setAnoLetivo(turmaDestino.getAnoLetivo());
            novaMatricula.setSerie(turmaDestino.getSerie());
            novaMatricula.setTurno(turmaDestino.getTurno());
            novaMatricula.setDataMatricula(LocalDate.now());
            novaMatricula.setSituacao(SituacaoMatricula.ATIVA);
            matriculaRepository.save(novaMatricula);
            // var nova = new Matricula();
            // nova.setNumero(gerarNumero(turmaDestino.getAnoLetivo()));
            // nova.setAluno(aluno);
            // nova.setTurma(turmaDestino);
            // nova.setAnoLetivo(turmaDestino.getAnoLetivo());
            // nova.setSerie(turmaDestino.getSerie());
            // nova.setTurno(turmaDestino.getTurno());
            // nova.setDataMatricula(LocalDate.now());
            // nova.setSituacao(SituacaoMatricula.ATIVA);
            // matriculaRepository.save(nova);
            matriculados++;
        }

        return new RematriculaResult(total, matriculados, ignorados.size(), ignorados);
    }

    /**
     * Gera o próximo número de matrícula consultando o MAX do ano no banco.
     * Thread-safe via @Transactional na operação de criar.
     */
    private String gerarNumero(int anoLetivo) {
        String ano = String.valueOf(anoLetivo);
        Integer max = matriculaRepository.findMaxNumeroByAno(ano);
        return "MAT" + ano + String.format("%05d", (max == null ? 0 : max) + 1);
    }

    private Matricula findOrThrow(Integer id) {
        return matriculaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Matrícula não encontrada"));
    }
}
