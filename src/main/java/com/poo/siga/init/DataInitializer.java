package com.poo.siga.init;

import com.poo.siga.model.*;
import com.poo.siga.model.enums.*;
import com.poo.siga.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final ProfessorRepository professorRepository;
    private final AlunoRepository alunoRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final TurmaRepository turmaRepository;
    private final TurmaDisciplinaProfessorRepository tdpRepository;
    private final MatriculaRepository matriculaRepository;
    private final AnoLetivoRepository anoLetivoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            log.info("Dados já existem. Seed ignorado.");
            return;
        }
        log.info("=== Iniciando seed da Escola Horizonte do Saber ===");

        criarAnosLetivos();
        var disciplinas = criarDisciplinas();
        var professores = criarProfessores();
        criarUsuarios(professores);
        var turmas2026 = criarTurmas(professores, 2026);
        var turmas2025 = criarTurmas(professores, 2025);
        var alunos = criarAlunos();
        matricularAlunos(alunos, turmas2026, 2026);
        matricularAlunos(alunos, turmas2025, 2025);

        log.info("=== Seed finalizado: {} alunos, {} professores, {} turmas, {} disciplinas ===",
            alunos.size(), professores.size(), turmas2026.size() + turmas2025.size(), disciplinas.size());
    }

    private void criarAnosLetivos() {
        var al2025 = new AnoLetivo();
        al2025.setAno(2025);
        al2025.setDataInicio(LocalDate.of(2025, 2, 5));
        al2025.setDataEncerramento(LocalDate.of(2025, 12, 15));
        al2025.setEncerrado(true);
        anoLetivoRepository.save(al2025);

        var al2026 = new AnoLetivo();
        al2026.setAno(2026);
        al2026.setDataInicio(LocalDate.of(2026, 2, 4));
        al2026.setDataEncerramento(LocalDate.of(2026, 12, 14));
        al2026.setEncerrado(false);
        anoLetivoRepository.save(al2026);
    }

    private List<Disciplina> criarDisciplinas() {
        String[][] disc = {
            {"Português", "200"}, {"Matemática", "200"}, {"Ciências", "120"},
            {"História", "120"}, {"Geografia", "120"}, {"Educação Física", "80"},
            {"Artes", "80"}, {"Informática", "80"}
        };
        List<Disciplina> lista = new ArrayList<>();
        for (int i = 0; i < disc.length; i++) {
            var d = new Disciplina();
            d.setCodigo("DISC" + String.format("%04d", i + 1));
            d.setDescricao(disc[i][0]);
            d.setCargaHorariaAnual(Integer.parseInt(disc[i][1]));
            lista.add(disciplinaRepository.save(d));
        }
        log.info("✅ {} disciplinas criadas", lista.size());
        return lista;
    }

    private List<Professor> criarProfessores() {
        String[][] dados = {
            {"Ana Silva Oliveira", "ana.oliveira@horizonte.edu.br", "11122233344"},
            {"Bruno Costa Lima", "bruno.lima@horizonte.edu.br", "22233344455"},
            {"Carla Mendes Souza", "carla.souza@horizonte.edu.br", "33344455566"},
            {"Daniel Ferreira Santos", "daniel.santos@horizonte.edu.br", "44455566677"},
            {"Elena Rodrigues Alves", "elena.alves@horizonte.edu.br", "55566677788"},
            {"Fernando Gomes Pereira", "fernando.pereira@horizonte.edu.br", "66677788899"},
            {"Gabriela Martins Costa", "gabriela.costa@horizonte.edu.br", "77788899900"},
            {"Henrique Almeida Dias", "henrique.dias@horizonte.edu.br", "88899900011"},
            {"Isabela Nascimento", "isabela.nasc@horizonte.edu.br", "99900011122"},
            {"José Carlos Barbosa", "jose.barbosa@horizonte.edu.br", "10011122233"},
            {"Karen Lopes Vieira", "karen.vieira@horizonte.edu.br", "11122233355"},
            {"Lucas Ramos Freitas", "lucas.freitas@horizonte.edu.br", "22233344466"},
            {"Maria Helena Cardoso", "maria.cardoso@horizonte.edu.br", "33344455577"},
            {"Nelson Teixeira Pinto", "nelson.pinto@horizonte.edu.br", "44455566688"},
            {"Patricia Rocha Melo", "patricia.melo@horizonte.edu.br", "55566677799"}
        };
        List<Professor> lista = new ArrayList<>();
        for (int i = 0; i < dados.length; i++) {
            var p = new Professor();
            p.setCodigoFuncional("RF" + String.format("%06d", i + 1));
            p.setNome(dados[i][0]);
            p.setEmail(dados[i][1]);
            p.setCpf(dados[i][2]);
            p.setDataNascimento(LocalDate.of(1975 + (i % 20), (i % 12) + 1, (i % 28) + 1));
            lista.add(professorRepository.save(p));
        }
        log.info("✅ {} professores criados", lista.size());
        return lista;
    }

    private void criarUsuarios(List<Professor> professores) {
        String senhaPadrao = passwordEncoder.encode("siga2026");

        // 4 Administradores
        criarUser("Carlos Henrique Almeida", "carlos.almeida@horizonte.edu.br", "admin", senhaPadrao, Role.ADMIN);
        criarUser("Marcos Vieira Souza", "marcos.souza@horizonte.edu.br", "admin2", senhaPadrao, Role.ADMIN);
        criarUser("Renata Oliveira", "renata.oliveira@horizonte.edu.br", "admin3", senhaPadrao, Role.ADMIN);
        criarUser("Administrador Sistema", "admin@horizonte.edu.br", "sysadmin", senhaPadrao, Role.ADMIN);

        // 2 Coordenadores
        criarUser("Juliana Araújo Lima", "juliana.lima@horizonte.edu.br", "coord1", senhaPadrao, Role.COORDENADOR);
        criarUser("Ricardo Santos Moura", "ricardo.moura@horizonte.edu.br", "coord2", senhaPadrao, Role.COORDENADOR);

        // 3 Operadores
        criarUser("Sandra Machado Silva", "sandra.silva@horizonte.edu.br", "oper1", senhaPadrao, Role.OPERADOR);
        criarUser("Paulo Roberto Cunha", "paulo.cunha@horizonte.edu.br", "oper2", senhaPadrao, Role.OPERADOR);
        criarUser("Tatiana Fonseca", "tatiana.fonseca@horizonte.edu.br", "oper3", senhaPadrao, Role.OPERADOR);

        // Professores (vinculados)
        for (int i = 0; i < professores.size(); i++) {
            var p = professores.get(i);
            var u = new Usuario();
            u.setCodigo("USR" + String.format("%05d", i + 10));
            u.setNomeCompleto(p.getNome());
            u.setEmail(p.getEmail());
            u.setLogin("prof" + (i + 1));
            u.setSenha(senhaPadrao);
            u.setRole(Role.PROFESSOR);
            u.setProfessor(p);
            u.setDataCadastro(LocalDateTime.now());
            u.setDataExpiracaoSenha(LocalDate.now().plusDays(90));
            u.setPrimeiroAcesso(false);
            usuarioRepository.save(u);
        }
        log.info("✅ {} usuários criados", 9 + professores.size());
    }

    private Usuario criarUser(String nome, String email, String login, String senha, Role role) {
        var u = new Usuario();
        u.setCodigo("USR" + String.format("%05d", new Random().nextInt(90000) + 10000));
        u.setNomeCompleto(nome);
        u.setEmail(email);
        u.setLogin(login);
        u.setSenha(senha);
        u.setRole(role);
        u.setDataCadastro(LocalDateTime.now());
        u.setDataExpiracaoSenha(LocalDate.now().plusDays(90));
        u.setPrimeiroAcesso(false);
        return usuarioRepository.save(u);
    }

    private List<Turma> criarTurmas(List<Professor> professores, int anoLetivo) {
        List<Turma> turmas = new ArrayList<>();
        String[] nomes = {"A", "B"};
        Turno[] turnos = {Turno.MATUTINO, Turno.VESPERTINO};
        int profIdx = 0;

        for (int serie = 1; serie <= 5; serie++) {
            for (int t = 0; t < 2; t++) {
                var turma = new Turma();
                turma.setCodigo("TUR" + anoLetivo + serie + nomes[t]);
                turma.setAnoLetivo(anoLetivo);
                turma.setSerie(serie);
                turma.setNome(nomes[t]);
                turma.setTurno(turnos[t]);
                turma.setProfessorRegente(professores.get(profIdx % professores.size()));
                turmas.add(turmaRepository.save(turma));
                profIdx++;
            }
        }
        log.info("✅ {} turmas criadas para {}", turmas.size(), anoLetivo);
        return turmas;
    }

    private List<Aluno> criarAlunos() {
        String[] nomes = {"Ana", "Bruno", "Carla", "Diego", "Eduarda", "Felipe", "Gabriela", "Henrique",
            "Isabela", "João", "Karina", "Lucas", "Marina", "Nicolas", "Olivia", "Pedro",
            "Rafaela", "Samuel", "Tatiana", "Vinicius", "Yasmin", "Arthur", "Beatriz", "Caio",
            "Daniela", "Emanuel", "Fernanda", "Gustavo", "Helena", "Igor"};
        String[] sobrenomes = {"Silva", "Santos", "Oliveira", "Souza", "Lima", "Pereira", "Costa",
            "Rodrigues", "Almeida", "Nascimento"};

        List<Aluno> alunos = new ArrayList<>();
        Random rng = new Random(42);
        for (int i = 0; i < 280; i++) {
            var a = new Aluno();
            String nome = nomes[i % nomes.length] + " " + sobrenomes[i % sobrenomes.length] +
                (i >= 30 ? " " + sobrenomes[(i + 3) % sobrenomes.length] : "");
            a.setRa("RA" + String.format("%06d", i + 1));
            a.setNome(nome);
            a.setDataNascimento(LocalDate.of(2014 + (i % 6), (i % 12) + 1, (i % 28) + 1));
            a.setCpf(String.format("%011d", 50000000000L + i));
            a.setEmail("aluno" + (i + 1) + "@horizonte.edu.br");
            a.setNomeResponsavel("Resp. de " + nome.split(" ")[0]);
            a.setCpfResponsavel(String.format("%011d", 70000000000L + i));
            alunos.add(alunoRepository.save(a));
        }
        log.info("✅ {} alunos criados", alunos.size());
        return alunos;
    }

    private void matricularAlunos(List<Aluno> alunos, List<Turma> turmas, int anoLetivo) {
        // Distribute 280 students across 10 turmas (~28 per turma)
        int alunosPorTurma = alunos.size() / turmas.size();
        int extra = alunos.size() % turmas.size();
        int idx = 0;
        int matNum = 0;
        for (int t = 0; t < turmas.size(); t++) {
            Turma turma = turmas.get(t);
            int count = alunosPorTurma + (t < extra ? 1 : 0);
            for (int a = 0; a < count && idx < alunos.size(); a++, idx++) {
                var m = new Matricula();
                matNum++;
                m.setNumero("MAT" + anoLetivo + String.format("%05d", matNum));
                m.setAluno(alunos.get(idx));
                m.setTurma(turma);
                m.setAnoLetivo(anoLetivo);
                m.setSerie(turma.getSerie());
                m.setTurno(turma.getTurno());
                m.setDataMatricula(LocalDate.of(anoLetivo, 1, 15));
                m.setSituacao(SituacaoMatricula.ATIVA);
                matriculaRepository.save(m);
            }
        }
        log.info("✅ {} matrículas criadas para {}", matNum, anoLetivo);
    }
}
