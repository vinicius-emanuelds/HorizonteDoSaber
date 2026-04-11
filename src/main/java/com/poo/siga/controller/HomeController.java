package com.poo.siga.controller;

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
    public String home(Model model) {
        model.addAttribute("pageTitle", "Dashboard");
        return "home";
    }

    @GetMapping("/aluno")
    public String aluno(Model model) {
        model.addAttribute("pageTitle", "Alunos");
        return "aluno";
    }

    @GetMapping("/professor")
    public String professor(Model model) {
        model.addAttribute("pageTitle", "Professores");
        return "professor";
    }

    @GetMapping("/disciplina")
    public String disciplina(Model model) {
        model.addAttribute("pageTitle", "Disciplinas");
        return "disciplina";
    }

    @GetMapping("/turma")
    public String turma(Model model) {
        model.addAttribute("pageTitle", "Turmas");
        return "turma";
    }

    @GetMapping("/matricula")
    public String matricula(Model model) {
        model.addAttribute("pageTitle", "Matrículas");
        return "matricula";
    }

    @GetMapping("/nota")
    public String nota(Model model) {
        model.addAttribute("pageTitle", "Notas");
        return "nota";
    }

    @GetMapping("/frequencia")
    public String frequencia(Model model) {
        model.addAttribute("pageTitle", "Frequência");
        return "frequencia";
    }

    @GetMapping("/usuario")
    public String usuario(Model model) {
        model.addAttribute("pageTitle", "Usuários");
        return "usuario";
    }

    @GetMapping("/turma-detalhe")
    public String turmaDetalhe(Model model) {
        model.addAttribute("pageTitle", "Diário de Classe");
        return "turma-detalhe";
    }

    @GetMapping("/relatorios")
    public String relatorios(Model model) {
        model.addAttribute("pageTitle", "Relatórios e Históricos");
        return "relatorios";
    }
}
