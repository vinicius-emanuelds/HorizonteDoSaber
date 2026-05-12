package com.poo.siga.service;

import com.poo.siga.dto.nota.*;
import com.poo.siga.model.Nota;
import com.poo.siga.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotaService {

    private final NotaRepository notaRepository;
    private final TurmaRepository turmaRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final AlunoRepository alunoRepository;
    private final AnoLetivoService anoLetivoService;

    @Transactional(readOnly = true)
    public List<NotaResponse> listarPorTurmaEDisciplina(Integer turmaId, Integer disciplinaId, Integer periodo) {
        return notaRepository.findByTurmaIdAndDisciplinaIdAndPeriodo(turmaId, disciplinaId, periodo)
                .stream().map(NotaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<NotaResponse> listarPorAluno(Integer alunoId) {
        return notaRepository.findByAlunoId(alunoId).stream().map(NotaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<NotaResponse> listarPorTurma(Integer turmaId) {
        return notaRepository.findByTurmaId(turmaId).stream().map(NotaResponse::from).toList();
    }

    @Transactional
    public NotaResponse lancar(NotaRequest req, String usuarioLogin) {
        var turma = turmaRepository.findById(req.turmaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma não encontrada"));

        // Regra r: bloqueia lançamentos quando o ano letivo está encerrado
        anoLetivoService.validarAnoAberto(turma.getAnoLetivo());

        var disciplina = disciplinaRepository.findById(req.disciplinaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Disciplina não encontrada"));
        var aluno = alunoRepository.findById(req.alunoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado"));

        // Arredondar para 1 casa decimal
        double valorArredondado = Math.round(req.valor() * 10.0) / 10.0;

        var nota = new Nota();
        nota.setTurma(turma);
        nota.setDisciplina(disciplina);
        nota.setAluno(aluno);
        nota.setPeriodo(req.periodo());
        nota.setTipoAvaliacao(req.tipoAvaliacao());
        nota.setValor(valorArredondado);
        nota.setDataLancamento(LocalDateTime.now());
        nota.setUsuarioLancamento(usuarioLogin);
        return NotaResponse.from(notaRepository.save(nota));
    }

    @Transactional
    public NotaResponse atualizar(Integer id, Double novoValor, String usuarioLogin) {
        var nota = notaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nota não encontrada"));
        // Regra r: bloqueia alterações quando o ano letivo está encerrado
        anoLetivoService.validarAnoAberto(nota.getTurma().getAnoLetivo());
        nota.setValor(Math.round(novoValor * 10.0) / 10.0);
        nota.setDataLancamento(LocalDateTime.now());
        nota.setUsuarioLancamento(usuarioLogin);
        return NotaResponse.from(notaRepository.save(nota));
    }

    /**
     * Calcula a média bimestral conforme regras de negócio:
     * Se (AV1 + AV2) / 2 >= 5.0 => média = (AV1 + AV2) / 2
     * Senão, se REC existe => média = max(média AV, REC)
     */
    public Double calcularMediaBimestral(Integer turmaId, Integer disciplinaId, Integer alunoId, Integer periodo) {
        var notas = notaRepository.buscarNotasDoPeriodo(turmaId, disciplinaId, alunoId, periodo);
        Double av1 = null, av2 = null, rec = null;
        for (var n : notas) {
            switch (n.getTipoAvaliacao()) {
                case AV1 -> av1 = n.getValor();
                case AV2 -> av2 = n.getValor();
                case REC -> rec = n.getValor();
            }
        }
        if (av1 == null || av2 == null) return null;
        double mediaAv = (av1 + av2) / 2.0;
        if (mediaAv >= 5.0) return Math.round(mediaAv * 10.0) / 10.0;
        if (rec != null) {
            return Math.round(Math.max(mediaAv, rec) * 10.0) / 10.0;
        }
        return Math.round(mediaAv * 10.0) / 10.0;
    }

    /**
     * Calcula a média anual: média aritmética das 4 médias bimestrais
     */
    public Double calcularMediaAnual(Integer turmaId, Integer disciplinaId, Integer alunoId) {
        double soma = 0;
        int count = 0;
        for (int p = 1; p <= 4; p++) {
            Double mb = calcularMediaBimestral(turmaId, disciplinaId, alunoId, p);
            if (mb != null) {
                soma += mb;
                count++;
            }
        }
        if (count < 4) return null;
        return Math.round((soma / 4.0) * 10.0) / 10.0;
    }
}
