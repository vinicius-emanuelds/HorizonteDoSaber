package com.poo.siga.service;

import com.poo.siga.dto.disciplina.*;
import com.poo.siga.model.Disciplina;
import com.poo.siga.repository.DisciplinaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class DisciplinaService {

    private final DisciplinaRepository repository;

    @Transactional(readOnly = true)
    public Page<DisciplinaResponse> listar(String descricao, String codigo, Boolean ativo, Pageable pageable) {
        return repository.buscarComFiltros(descricao, codigo, ativo, pageable).map(DisciplinaResponse::from);
    }

    @Transactional(readOnly = true)
    public DisciplinaResponse buscarPorId(Integer id) {
        return DisciplinaResponse.from(findOrThrow(id));
    }

    @Transactional
    public DisciplinaResponse criar(DisciplinaRequest req) {
        var d = new Disciplina();
        d.setDescricao(req.descricao());
        d.setCargaHorariaAnual(req.cargaHorariaAnual());
        return DisciplinaResponse.from(repository.save(d));
    }

    @Transactional
    public DisciplinaResponse atualizar(Integer id, DisciplinaRequest req) {
        var d = findOrThrow(id);
        d.setDescricao(req.descricao());
        d.setCargaHorariaAnual(req.cargaHorariaAnual());
        return DisciplinaResponse.from(repository.save(d));
    }

    @Transactional
    public void inativar(Integer id) {
        var d = findOrThrow(id);
        d.setAtivo(false);
        repository.save(d);
    }

    @Transactional
    public void excluir(Integer id) {
        findOrThrow(id);
        repository.deleteById(id);
    }

    private Disciplina findOrThrow(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Disciplina não encontrada"));
    }
}
