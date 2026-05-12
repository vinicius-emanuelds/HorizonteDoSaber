package com.poo.siga.controller;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String home(Model model, Authentication auth) {
        model.addAttribute("pageTitle", "Dashboard");
        adicionarRoles(model, auth);
        return "home";
    }

    @GetMapping("/aluno")
    public String aluno(Model model, Authentication auth) {
        model.addAttribute("pageTitle", "Alunos");
        adicionarRoles(model, auth);
        return "aluno";
    }

    @GetMapping("/professor")
    public String professor(Model model, Authentication auth) {
        model.addAttribute("pageTitle", "Professores");
        adicionarRoles(model, auth);
        return "professor";
    }

    @GetMapping("/disciplina")
    public String disciplina(Model model, Authentication auth) {
        model.addAttribute("pageTitle", "Disciplinas");
        adicionarRoles(model, auth);
        return "disciplina";
    }

    @GetMapping("/turma")
    public String turma(Model model, Authentication auth) {
        model.addAttribute("pageTitle", "Turmas");
        adicionarRoles(model, auth);
        return "turma";
    }

    @GetMapping("/matricula")
    public String matricula(Model model, Authentication auth) {
        model.addAttribute("pageTitle", "Matrículas");
        adicionarRoles(model, auth);
        return "matricula";
    }

    @GetMapping("/nota")
    public String nota(Model model, Authentication auth) {
        model.addAttribute("pageTitle", "Notas");
        adicionarRoles(model, auth);
        return "nota";
    }

    @GetMapping("/frequencia")
    public String frequencia(Model model, Authentication auth) {
        model.addAttribute("pageTitle", "Frequência");
        adicionarRoles(model, auth);
        return "frequencia";
    }

    @GetMapping("/usuario")
    public String usuario(Model model, Authentication auth) {
        model.addAttribute("pageTitle", "Usuários");
        adicionarRoles(model, auth);
        return "usuario";
    }

    @GetMapping("/turma-detalhe")
    public String turmaDetalhe(Model model, Authentication auth) {
        model.addAttribute("pageTitle", "Diário de Classe");
        adicionarRoles(model, auth);
        return "turma-detalhe";
    }

    @GetMapping("/relatorios")
    public String relatorios(Model model, Authentication auth) {
        model.addAttribute("pageTitle", "Relatórios e Históricos");
        adicionarRoles(model, auth);
        return "relatorios";
    }

    @GetMapping("/ano-letivo")
    public String anoLetivo(Model model, Authentication auth) {
        model.addAttribute("pageTitle", "Ano Letivo");
        adicionarRoles(model, auth);
        return "ano-letivo";
    }

    @GetMapping("/modelos-grade")
    public String modelosGrade(Model model, Authentication auth) {
        model.addAttribute("pageTitle", "Modelos de Grade");
        adicionarRoles(model, auth);
        return "modelos-grade";
    }

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
