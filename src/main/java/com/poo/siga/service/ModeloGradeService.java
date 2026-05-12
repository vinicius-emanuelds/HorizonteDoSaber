package com.poo.siga.service;

import com.poo.siga.dto.grade.ModeloGradeRequest;
import com.poo.siga.dto.grade.ModeloGradeResponse;
import com.poo.siga.model.Disciplina;
import com.poo.siga.model.ModeloGrade;
import com.poo.siga.model.ModeloGradeAula;
import com.poo.siga.repository.DisciplinaRepository;
import com.poo.siga.repository.ModeloGradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModeloGradeService {

    private final ModeloGradeRepository repository;
    private final DisciplinaRepository disciplinaRepository;
    private final AnoLetivoService anoLetivoService;

    @Transactional(readOnly = true)
    public List<ModeloGradeResponse> listarTodos() {
        return repository.findAll().stream().map(ModeloGradeResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ModeloGradeResponse> listarPorAno(Integer anoLetivo) {
        return repository.findByAnoLetivoOrderBySerieAscNomeAsc(anoLetivo)
                .stream().map(ModeloGradeResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ModeloGradeResponse buscarPorId(Integer id) {
        return ModeloGradeResponse.from(findOrThrow(id));
    }

    @Transactional
    public ModeloGradeResponse criar(ModeloGradeRequest req) {
        anoLetivoService.validarAnoAberto(req.anoLetivo());
        
        var modelo = new ModeloGrade();
        modelo.setAnoLetivo(req.anoLetivo());
        modelo.setSerie(req.serie());
        modelo.setNome(req.nome());
        
        preencherAulas(modelo, req.aulas());
        return ModeloGradeResponse.from(repository.save(modelo));
    }

    @Transactional
    public ModeloGradeResponse atualizar(Integer id, ModeloGradeRequest req) {
        anoLetivoService.validarAnoAberto(req.anoLetivo());
        
        var modelo = findOrThrow(id);
        modelo.setAnoLetivo(req.anoLetivo());
        modelo.setSerie(req.serie());
        modelo.setNome(req.nome());
        
        preencherAulas(modelo, req.aulas());
        return ModeloGradeResponse.from(repository.save(modelo));
    }

    @Transactional
    public void deletar(Integer id) {
        repository.delete(findOrThrow(id));
    }

    private void preencherAulas(ModeloGrade modelo, List<ModeloGradeRequest.AulaRequest> aulasReq) {
        if (modelo.getAulas() == null) {
            modelo.setAulas(new ArrayList<>());
        } else {
            modelo.getAulas().clear();
            repository.saveAndFlush(modelo);
        }
        
        if (aulasReq != null) {
            for (var req : aulasReq) {
                Disciplina d = disciplinaRepository.findById(req.disciplinaId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Disciplina não encontrada: " + req.disciplinaId()));
                
                var aula = new ModeloGradeAula();
                aula.setModeloGrade(modelo);
                aula.setDisciplina(d);
                aula.setDiaSemana(req.diaSemana());
                aula.setNumeroAula(req.numeroAula());
                modelo.getAulas().add(aula);
            }
        }
    }

    public ModeloGrade findOrThrow(Integer id) {
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Modelo de grade não encontrado"));
    }
}
