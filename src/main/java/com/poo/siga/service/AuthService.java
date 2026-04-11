package com.poo.siga.service;

import com.poo.siga.dto.auth.*;
import com.poo.siga.model.LogAcesso;
import com.poo.siga.repository.LogAcessoRepository;
import com.poo.siga.repository.UsuarioRepository;
import com.poo.siga.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final LogAcessoRepository logRepository;

    @Transactional
    public JwtResponse login(LoginRequest req) {
        var usuario = usuarioRepository.findByLogin(req.login())
                .orElseThrow(() -> {
                    registrarLog(req.login(), "LOGIN", "FALHA", "Usuário não encontrado");
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
                });

        if (usuario.isBloqueado()) {
            registrarLog(req.login(), "LOGIN", "FALHA", "Usuário bloqueado");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Usuário bloqueado. Contate o administrador para desbloquear.");
        }

        if (!usuario.isAtivo()) {
            registrarLog(req.login(), "LOGIN", "FALHA", "Usuário inativo");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário inativo");
        }

        if (!passwordEncoder.matches(req.senha(), usuario.getSenha())) {
            int tentativas = usuario.getTentativasLogin() + 1;
            usuario.setTentativasLogin(tentativas);
            if (tentativas >= 5) {
                usuario.setBloqueado(true);
                registrarLog(req.login(), "LOGIN", "BLOQUEADO",
                    "5 tentativas inválidas - usuário bloqueado");
            } else {
                registrarLog(req.login(), "LOGIN", "FALHA",
                    "Tentativa " + tentativas + "/5");
            }
            usuarioRepository.save(usuario);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }

        // Login com sucesso - resetar tentativas
        usuario.setTentativasLogin(0);
        usuarioRepository.save(usuario);

        registrarLog(req.login(), "LOGIN", "SUCESSO", null);

        String token = jwtUtil.generate(usuario.getLogin(), usuario.getRole().name());
        return new JwtResponse(token, usuario.getRole().name(), usuario.getLogin(),
                usuario.getNomeCompleto(), usuario.isPrimeiroAcesso());
    }

    @Transactional
    public void trocarSenha(String login, String senhaAtual, String novaSenha) {
        var usuario = usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        if (!passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Senha atual incorreta");
        }
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuario.setPrimeiroAcesso(false);
        usuario.setDataExpiracaoSenha(java.time.LocalDate.now().plusDays(90));
        usuarioRepository.save(usuario);
        registrarLog(login, "TROCA_SENHA", "SUCESSO", null);
    }

    private void registrarLog(String usuario, String operacao, String resultado, String detalhes) {
        var log = new LogAcesso();
        log.setUsuario(usuario);
        log.setOperacao(operacao);
        log.setResultado(resultado);
        log.setDetalhes(detalhes);
        logRepository.save(log);
    }
}
