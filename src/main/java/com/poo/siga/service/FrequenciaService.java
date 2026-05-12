package com.poo.siga.service;

import com.poo.siga.dto.frequencia.*;
import com.poo.siga.model.Frequencia;
import com.poo.siga.model.enums.StatusFrequencia;
import com.poo.siga.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FrequenciaService {

    private final FrequenciaRepository frequenciaRepository;
    private final TurmaRepository turmaRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final AlunoRepository alunoRepository;
    private final AnoLetivoService anoLetivoService;
    private final GradeHorariaRepository gradeHorariaRepository;

    @Transactional(readOnly = true)
    public List<FrequenciaResponse> listarPorTurmaEData(Integer turmaId, Integer disciplinaId, String data) {
        var date = java.time.LocalDate.parse(data);
        return frequenciaRepository.findByTurmaIdAndDisciplinaIdAndData(turmaId, disciplinaId, date)
                .stream().map(FrequenciaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<FrequenciaResponse> listarPorAluno(Integer alunoId) {
        return frequenciaRepository.findByAlunoId(alunoId).stream().map(FrequenciaResponse::from).toList();
    }

    @Transactional
    public FrequenciaResponse registrar(FrequenciaRequest req) {
        var turma = turmaRepository.findById(req.turmaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma não encontrada"));

        // Regra r: bloqueia lançamentos quando o ano letivo está encerrado
        anoLetivoService.validarAnoAberto(turma.getAnoLetivo());

        var disciplina = disciplinaRepository.findById(req.disciplinaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Disciplina não encontrada"));
        var aluno = alunoRepository.findById(req.alunoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado"));

        // Validar se a disciplina está na grade horária da turma para aquele dia e número de aula
        var gradeOpt = gradeHorariaRepository.findByTurmaIdAndDiaSemanaAndNumeroAula(
                turma.getId(), req.data().getDayOfWeek(), req.numeroAula());
        if (gradeOpt.isEmpty() || !gradeOpt.get().getDisciplina().getId().equals(disciplina.getId())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, 
                "A aula " + req.numeroAula() + " de " + req.data().getDayOfWeek() + " não corresponde à disciplina " + disciplina.getDescricao() + " na grade desta turma.");
        }

        var f = new Frequencia();
        f.setTurma(turma);
        f.setDisciplina(disciplina);
        f.setAluno(aluno);
        f.setData(req.data());
        f.setNumeroAula(req.numeroAula());
        f.setStatus(req.status());
        f.setJustificativa(req.justificativa());
        return FrequenciaResponse.from(frequenciaRepository.save(f));
    }

    @Transactional
    public FrequenciaResponse atualizar(Integer id, FrequenciaRequest req) {
        var f = frequenciaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Frequência não encontrada"));
        // Regra r: bloqueia alterações quando o ano letivo está encerrado
        anoLetivoService.validarAnoAberto(f.getTurma().getAnoLetivo());
        f.setStatus(req.status());
        f.setJustificativa(req.justificativa());
        return FrequenciaResponse.from(frequenciaRepository.save(f));
    }

    /**
     * Calcula o percentual de presença do aluno em uma disciplina
     */
    public Double calcularPercentualPresenca(Integer alunoId, Integer disciplinaId) {
        long total = frequenciaRepository.countByAlunoIdAndDisciplinaId(alunoId, disciplinaId);
        if (total == 0) return null;
        long presencas = frequenciaRepository.countByAlunoIdAndDisciplinaIdAndStatus(
                alunoId, disciplinaId, StatusFrequencia.PRESENTE);
        long justificadas = frequenciaRepository.countByAlunoIdAndDisciplinaIdAndStatus(
                alunoId, disciplinaId, StatusFrequencia.JUSTIFICADO);
        // Justificadas contam como falta para cálculo, mas presença efetiva é somente PRESENTE
        return Math.round(((double) presencas / total) * 1000.0) / 10.0;
    }
}
