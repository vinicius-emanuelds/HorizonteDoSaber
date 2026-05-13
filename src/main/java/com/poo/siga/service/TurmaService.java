package com.poo.siga.service;

import com.poo.siga.dto.grade.GradeHorariaResponse;
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
    private final ModeloGradeRepository modeloGradeRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final GradeHorariaRepository gradeHorariaRepository;

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
        var modeloGrade = modeloGradeRepository.findById(req.modeloGradeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Modelo de grade não encontrado"));

        var turma = new Turma();
        turma.setCodigo(gerarCodigo());
        turma.setAnoLetivo(req.anoLetivo());
        turma.setSerie(req.serie());
        turma.setNome(req.nome());
        turma.setTurno(req.turno());
        turma.setProfessorRegente(professor);
        turma.setModeloGrade(modeloGrade);
        
        turma = turmaRepository.save(turma);
        
        preencherGradeHorariaEProfessores(turma, req.professoresEspecificos(), professor);
        
        return TurmaResponse.from(turma);
    }

    private String gerarCodigo() {
        Integer max = turmaRepository.findMaxCodigoNumber();
        return "TUR" + String.format("%05d", (max == null ? 0 : max) + 1);
    }

    @Transactional
    public TurmaResponse atualizar(Integer id, TurmaRequest req) {
        var turma = findOrThrow(id);
        var professor = professorRepository.findById(req.professorRegenteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Professor regente não encontrado"));
        var modeloGrade = modeloGradeRepository.findById(req.modeloGradeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Modelo de grade não encontrado"));

        turma.setAnoLetivo(req.anoLetivo());
        turma.setSerie(req.serie());
        turma.setNome(req.nome());
        turma.setTurno(req.turno());
        turma.setProfessorRegente(professor);
        turma.setModeloGrade(modeloGrade);

        // Limpa grade e professores específicos se o modelo for alterado
        gradeHorariaRepository.deleteByTurma(turma);
        if (turma.getDisciplinasProfessores() != null) {
            turma.getDisciplinasProfessores().clear();
        } else {
            turma.setDisciplinasProfessores(new java.util.ArrayList<>());
        }
        
        turma = turmaRepository.save(turma);
        preencherGradeHorariaEProfessores(turma, req.professoresEspecificos(), professor);

        return TurmaResponse.from(turma);
    }

    private void preencherGradeHorariaEProfessores(Turma turma, List<TurmaRequest.ProfessorEspecificoRequest> profsEspecificos, com.poo.siga.model.Professor regente) {
        var modelo = turma.getModeloGrade();
        if (modelo == null || modelo.getAulas() == null) return;
        
        // Mapeia professores específicos
        var mapaEspecificos = new java.util.HashMap<Integer, com.poo.siga.model.Professor>();
        if (profsEspecificos != null) {
            for (var pReq : profsEspecificos) {
                var p = professorRepository.findById(pReq.professorId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Professor não encontrado"));
                mapaEspecificos.put(pReq.disciplinaId(), p);
            }
        }
        
        // Mantem rastreio das disciplinas associadas
        var disciplinasTurma = new java.util.HashSet<Integer>();

        if (turma.getDisciplinasProfessores() == null) {
            turma.setDisciplinasProfessores(new java.util.ArrayList<>());
        }
        
        for (var aulaModelo : modelo.getAulas()) {
            var disc = aulaModelo.getDisciplina();
            
            // Criar a Grade Horaria para a Turma
            var grade = new com.poo.siga.model.GradeHoraria();
            grade.setTurma(turma);
            grade.setDisciplina(disc);
            grade.setDiaSemana(aulaModelo.getDiaSemana());
            grade.setNumeroAula(aulaModelo.getNumeroAula());
            gradeHorariaRepository.save(grade);
            
            // Se ainda não adicionamos esta disciplina em TurmaDisciplinaProfessor
            if (!disciplinasTurma.contains(disc.getId())) {
                disciplinasTurma.add(disc.getId());
                var turmaPorDisciplinaEProfessor = new com.poo.siga.model.TurmaDisciplinaProfessor();
                turmaPorDisciplinaEProfessor.setTurma(turma);
                turmaPorDisciplinaEProfessor.setDisciplina(disc);
                // Usa o professor específico se mapeado, senão usa o regente
                turmaPorDisciplinaEProfessor.setProfessor(mapaEspecificos.containsKey(disc.getId()) ? mapaEspecificos.get(disc.getId()) : regente);
                turma.getDisciplinasProfessores().add(turmaPorDisciplinaEProfessor);
                // var tp = new com.poo.siga.model.TurmaDisciplinaProfessor();
                // tp.setTurma(turma);
                // tp.setDisciplina(disc);
                // // Usa o professor específico se mapeado, senão usa o regente
                // tp.setProfessor(mapaEspecificos.containsKey(disc.getId()) ? mapaEspecificos.get(disc.getId()) : regente);
                // turma.getDisciplinasProfessores().add(tp);
            }
        }
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

    @Transactional(readOnly = true)
    public List<GradeHorariaResponse> getGradeHoraria(Integer turmaId, String loginProfessor) {
        List<com.poo.siga.model.GradeHoraria> grade = gradeHorariaRepository.findByTurmaId(turmaId);
        
        // Se um login de professor for fornecido, filtra apenas as disciplinas que ele ministra
        if (loginProfessor != null && !loginProfessor.isBlank()) {
            // Busca o professor vinculado ao usuario com esse login
            var disciplinasPermitidas = gradeHorariaRepository.findDisciplinaIdsByTurmaAndProfessorLogin(turmaId, loginProfessor);
            if (!disciplinasPermitidas.isEmpty()) {
                grade = grade.stream()
                    .filter(g -> disciplinasPermitidas.contains(g.getDisciplina().getId()))
                    .toList();
            }
        }
        
        return grade.stream().map(GradeHorariaResponse::from).toList();
    }
}
