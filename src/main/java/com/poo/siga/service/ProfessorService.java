package com.poo.siga.service;

import com.poo.siga.dto.professor.*;
import com.poo.siga.model.Professor;
import com.poo.siga.repository.ProfessorRepository;
import com.poo.siga.repository.TurmaDisciplinaProfessorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProfessorService {

    private final ProfessorRepository repository;
    private final TurmaDisciplinaProfessorRepository tdpRepository;

    @Transactional(readOnly = true)
    public Page<ProfessorResponse> listar(String nome, String codigoFuncional, Boolean ativo, Pageable pageable) {
        return repository.buscarComFiltros(nome, codigoFuncional, ativo, pageable).map(ProfessorResponse::from);
    }

    @Transactional(readOnly = true)
    public ProfessorResponse buscarPorId(Integer id) {
        return ProfessorResponse.from(findOrThrow(id));
    }

    @Transactional
    public ProfessorResponse criar(ProfessorRequest req) {
        if (repository.existsByCpf(req.cpf())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF já cadastrado");
        }
        var professor = new Professor();
        professor.setNome(req.nome());
        professor.setDataNascimento(req.dataNascimento());
        professor.setCpf(req.cpf());
        professor.setEmail(req.email());
        return ProfessorResponse.from(repository.save(professor));
    }

    @Transactional
    public ProfessorResponse atualizar(Integer id, ProfessorRequest req) {
        var professor = findOrThrow(id);
        professor.setNome(req.nome());
        professor.setDataNascimento(req.dataNascimento());
        professor.setCpf(req.cpf());
        professor.setEmail(req.email());
        return ProfessorResponse.from(repository.save(professor));
    }

    @Transactional
    public void inativar(Integer id) {
        var professor = findOrThrow(id);
        professor.setAtivo(false);
        repository.save(professor);
    }

    @Transactional
    public void excluir(Integer id) {
        var professor = findOrThrow(id);
        if (!tdpRepository.findByProfessorId(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Não é possível excluir professor vinculado a turmas. Utilize a inativação.");
        }
        repository.delete(professor);
    }

    private Professor findOrThrow(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Professor não encontrado"));
    }
}
