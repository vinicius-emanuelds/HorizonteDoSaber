package com.poo.siga.service;

import com.poo.siga.dto.usuario.*;
import com.poo.siga.model.Usuario;
import com.poo.siga.model.enums.Role;
import com.poo.siga.repository.ProfessorRepository;
import com.poo.siga.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;
    private final ProfessorRepository professorRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listar(String nome, Role role, Boolean ativo, Pageable pageable) {
        return repository.buscarComFiltros(nome, role, ativo, pageable).map(UsuarioResponse::from);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Integer id) {
        return UsuarioResponse.from(findOrThrow(id));
    }

    @Transactional
    public UsuarioResponse criar(UsuarioRequest req) {
        if (repository.existsByLogin(req.login())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Login já existe");
        }
        var novoUsuario = new Usuario();
        novoUsuario.setCodigo(gerarCodigo());
        novoUsuario.setNomeCompleto(req.nomeCompleto());
        novoUsuario.setEmail(req.email());
        novoUsuario.setLogin(req.login());
        novoUsuario.setSenha(passwordEncoder.encode(req.senha()));
        novoUsuario.setRole(req.role());
        novoUsuario.setDataExpiracaoSenha(LocalDate.now().plusDays(90));
        novoUsuario.setPrimeiroAcesso(true);

        if (req.role() == Role.PROFESSOR && req.professorId() != null) {
            var professor = professorRepository.findById(req.professorId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Professor não encontrado"));
            novoUsuario.setProfessor(professor);
        }
        return UsuarioResponse.from(repository.save(novoUsuario));
        // var u = new Usuario();
        // u.setCodigo(gerarCodigo());
        // u.setNomeCompleto(req.nomeCompleto());
        // u.setEmail(req.email());
        // u.setLogin(req.login());
        // u.setSenha(passwordEncoder.encode(req.senha()));
        // u.setRole(req.role());
        // u.setDataExpiracaoSenha(LocalDate.now().plusDays(90));
        // u.setPrimeiroAcesso(true);

        // if (req.role() == Role.PROFESSOR && req.professorId() != null) {
        //     var professor = professorRepository.findById(req.professorId())
        //             .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Professor não encontrado"));
        //     u.setProfessor(professor);
        // }
        // return UsuarioResponse.from(repository.save(u));
    }

    private String gerarCodigo() {
        Integer max = repository.findMaxCodigoNumber();
        return "USR" + String.format("%05d", (max == null ? 0 : max) + 1);
    }

    @Transactional
    public UsuarioResponse atualizar(Integer id, UsuarioRequest req) {
        var usuario = findOrThrow(id);
        usuario.setNomeCompleto(req.nomeCompleto());
        usuario.setEmail(req.email());
        usuario.setLogin(req.login());
        usuario.setRole(req.role());
        if (req.professorId() != null) {
            var professor = professorRepository.findById(req.professorId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Professor não encontrado"));
            usuario.setProfessor(professor);
        }
        if (req.senha() != null && !req.senha().trim().isEmpty()) {
            usuario.setSenha(passwordEncoder.encode(req.senha()));
        }
        return UsuarioResponse.from(repository.save(usuario));
        // var u = findOrThrow(id);
        // u.setNomeCompleto(req.nomeCompleto());
        // u.setEmail(req.email());
        // u.setLogin(req.login());
        // u.setRole(req.role());
        // if (req.professorId() != null) {
        //     var professor = professorRepository.findById(req.professorId())
        //             .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Professor não encontrado"));
        //     u.setProfessor(professor);
        // }
        // if (req.senha() != null && !req.senha().trim().isEmpty()) {
        //     u.setSenha(passwordEncoder.encode(req.senha()));
        // }
        // return UsuarioResponse.from(repository.save(u));
    }

    @Transactional
    public void inativar(Integer id) {
        var usuario = findOrThrow(id);
        usuario.setAtivo(false);
        repository.save(usuario);
        // var u = findOrThrow(id);
        // u.setAtivo(false);
        // repository.save(u);
    }

    @Transactional
    public void desbloquear(Integer id) {
        var usuario = findOrThrow(id);
        usuario.setBloqueado(false);
        usuario.setTentativasLogin(0);
        repository.save(usuario);
        // var u = findOrThrow(id);
        // u.setBloqueado(false);
        // u.setTentativasLogin(0);
        // repository.save(u);
    }

    @Transactional
    public void redefinirSenha(Integer id, String novaSenha) {
        var usuario = findOrThrow(id);
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuario.setPrimeiroAcesso(true);
        usuario.setDataExpiracaoSenha(LocalDate.now().plusDays(1)); // Expira rápido para forçar troca
        repository.save(usuario);
        // var u = findOrThrow(id);
        // u.setSenha(passwordEncoder.encode(novaSenha));
        // u.setPrimeiroAcesso(true);
        // u.setDataExpiracaoSenha(LocalDate.now().plusDays(1)); // Expira rápido para forçar troca
        // repository.save(u);
    }

    private Usuario findOrThrow(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }
}
