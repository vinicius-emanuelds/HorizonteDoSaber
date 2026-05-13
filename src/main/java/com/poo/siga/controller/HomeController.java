package com.poo.siga.controller;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class HomeController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * Mapa de rota → título da página.
     * Para adicionar uma nova página basta incluir uma entrada aqui — sem novo
     * método.
     */
    private static final Map<String, String> PAGE_TITLES = Map.ofEntries(
            Map.entry("/", "Dashboard"),
            Map.entry("/aluno", "Alunos"),
            Map.entry("/professor", "Professores"),
            Map.entry("/disciplina", "Disciplinas"),
            Map.entry("/turma", "Turmas"),
            Map.entry("/matricula", "Matrículas"),
            Map.entry("/nota", "Notas"),
            Map.entry("/frequencia", "Frequência"),
            Map.entry("/usuario", "Usuários"),
            Map.entry("/turma-detalhe", "Diário de Classe"),
            Map.entry("/relatorios", "Relatórios e Históricos"));

    /**
     * Handler unificado para todas as rotas de página.
     * Elimina o boilerplate dos 11 métodos idênticos do original.
     */
    @GetMapping({ "/", "/aluno", "/professor", "/disciplina", "/turma",
            "/matricula", "/nota", "/frequencia", "/usuario",
            "/turma-detalhe", "/relatorios" })
    public String page(HttpServletRequest request, Model model, Authentication auth)
            throws NoResourceFoundException {
        String path = request.getServletPath();
        String titulo = PAGE_TITLES.get(path);
        if (titulo == null) {
            log.warn("Rota sem título configurado: {}", path);
            throw new NoResourceFoundException(null, path);
        }

        model.addAttribute("pageTitle", titulo);
        adicionarRoles(model, auth);

        // O nome do template corresponde ao path sem a barra inicial (ex: "/aluno" →
        // "aluno")
        return path.equals("/") ? "home" : path.substring(1);
    }

    // @GetMapping("/")
    // public String home(Model model, Authentication auth) {
    // model.addAttribute("pageTitle", "Dashboard");
    // adicionarRoles(model, auth);
    // return "home";
    // }

    // @GetMapping("/aluno")
    // public String aluno(Model model, Authentication auth) {
    // model.addAttribute("pageTitle", "Alunos");
    // adicionarRoles(model, auth);
    // return "aluno";
    // }

    // @GetMapping("/professor")
    // public String professor(Model model, Authentication auth) {
    // model.addAttribute("pageTitle", "Professores");
    // adicionarRoles(model, auth);
    // return "professor";
    // }

    // @GetMapping("/disciplina")
    // public String disciplina(Model model, Authentication auth) {
    // model.addAttribute("pageTitle", "Disciplinas");
    // adicionarRoles(model, auth);
    // return "disciplina";
    // }

    // @GetMapping("/turma")
    // public String turma(Model model, Authentication auth) {
    // model.addAttribute("pageTitle", "Turmas");
    // adicionarRoles(model, auth);
    // return "turma";
    // }

    // @GetMapping("/matricula")
    // public String matricula(Model model, Authentication auth) {
    // model.addAttribute("pageTitle", "Matrículas");
    // adicionarRoles(model, auth);
    // return "matricula";
    // }

    // @GetMapping("/nota")
    // public String nota(Model model, Authentication auth) {
    // model.addAttribute("pageTitle", "Notas");
    // adicionarRoles(model, auth);
    // return "nota";
    // }

    // @GetMapping("/frequencia")
    // public String frequencia(Model model, Authentication auth) {
    // model.addAttribute("pageTitle", "Frequência");
    // adicionarRoles(model, auth);
    // return "frequencia";
    // }

    // @GetMapping("/usuario")
    // public String usuario(Model model, Authentication auth) {
    // model.addAttribute("pageTitle", "Usuários");
    // adicionarRoles(model, auth);
    // return "usuario";
    // }

    // @GetMapping("/turma-detalhe")
    // public String turmaDetalhe(Model model, Authentication auth) {
    // model.addAttribute("pageTitle", "Diário de Classe");
    // adicionarRoles(model, auth);
    // return "turma-detalhe";
    // }

    // @GetMapping("/relatorios")
    // public String relatorios(Model model, Authentication auth) {
    // model.addAttribute("pageTitle", "Relatórios e Históricos");
    // adicionarRoles(model, auth);
    // return "relatorios";
    // }

    // @GetMapping("/ano-letivo")
    // public String anoLetivo(Model model, Authentication auth) {
    // model.addAttribute("pageTitle", "Ano Letivo");
    // adicionarRoles(model, auth);
    // return "ano-letivo";
    // }

    // @GetMapping("/modelos-grade")
    // public String modelosGrade(Model model, Authentication auth) {
    // model.addAttribute("pageTitle", "Modelos de Grade");
    // adicionarRoles(model, auth);
    // return "modelos-grade";
    // }

    private void adicionarRoles(Model model, Authentication auth) {
        Set<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        model.addAttribute("isAdmin", roles.contains("ROLE_ADMIN"));
        model.addAttribute("isCoordenador", roles.contains("ROLE_COORDENADOR"));
        model.addAttribute("isOperador", roles.contains("ROLE_OPERADOR"));
        model.addAttribute("isProfessor", roles.contains("ROLE_PROFESSOR"));
        model.addAttribute("usuarioNome", auth.getName());
    }
}
