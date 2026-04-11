package com.poo.siga.service;

import com.poo.siga.dto.aluno.*;
import com.poo.siga.model.Aluno;
import com.poo.siga.repository.AlunoRepository;
import com.poo.siga.repository.MatriculaRepository;
import com.poo.siga.repository.NotaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AlunoService {

    private final AlunoRepository repository;
    private final MatriculaRepository matriculaRepository;
    private final NotaRepository notaRepository;

    @Transactional(readOnly = true)
    public Page<AlunoResponse> listar(String nome, String ra, String cpf, Boolean ativo, Pageable pageable) {
        return repository.buscarComFiltros(nome, ra, cpf, ativo, pageable).map(AlunoResponse::from);
    }

    @Transactional(readOnly = true)
    public AlunoResponse buscarPorId(Integer id) {
        return AlunoResponse.from(findOrThrow(id));
    }

    @Transactional
    public AlunoResponse criar(AlunoRequest req) {
        if (repository.existsByCpf(req.cpf())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF do aluno já cadastrado");
        }
        var aluno = new Aluno();
        aluno.setNome(req.nome());
        aluno.setDataNascimento(req.dataNascimento());
        aluno.setCpf(req.cpf());
        aluno.setEmail(req.email());
        aluno.setNomeResponsavel(req.nomeResponsavel());
        aluno.setCpfResponsavel(req.cpfResponsavel());
        return AlunoResponse.from(repository.save(aluno));
    }

    @Transactional
    public AlunoResponse atualizar(Integer id, AlunoRequest req) {
        var aluno = findOrThrow(id);
        aluno.setNome(req.nome());
        aluno.setDataNascimento(req.dataNascimento());
        aluno.setCpf(req.cpf());
        aluno.setEmail(req.email());
        aluno.setNomeResponsavel(req.nomeResponsavel());
        aluno.setCpfResponsavel(req.cpfResponsavel());
        return AlunoResponse.from(repository.save(aluno));
    }

    @Transactional
    public void inativar(Integer id) {
        var aluno = findOrThrow(id);
        aluno.setAtivo(false);
        repository.save(aluno);
    }

    @Transactional
    public void ativar(Integer id) {
        var aluno = findOrThrow(id);
        aluno.setAtivo(true);
        repository.save(aluno);
    }

    @Transactional
    public void excluir(Integer id) {
        var aluno = findOrThrow(id);
        if (!matriculaRepository.findByAlunoId(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Não é possível excluir aluno com matrículas. Utilize a inativação.");
        }
        if (!notaRepository.findByAlunoId(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Não é possível excluir aluno com notas lançadas. Utilize a inativação.");
        }
        repository.delete(aluno);
    }

    private Aluno findOrThrow(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado"));
    }
}
