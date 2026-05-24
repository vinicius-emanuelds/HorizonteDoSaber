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
    private final ModeloGradeRepository modeloGradeRepository;
    private final GradeHorariaRepository gradeHorariaRepository;
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
        var modelos = criarModelosGrade(disciplinas);
        var professores = criarProfessores();
        criarUsuarios(professores);
        var turmas2026 = criarTurmas(professores, modelos, 2026);
        var turmas2025 = criarTurmas(professores, modelos, 2025);
        var alunos = criarAlunos();
        matricularAlunos(alunos, turmas2026, 2026);
        matricularAlunos(alunos, turmas2025, 2025);

        log.info("=== Seed finalizado: {} alunos, {} professores, {} turmas, {} disciplinas ===",
            alunos.size(), professores.size(), turmas2026.size() + turmas2025.size(), disciplinas.size());
    }

    private void criarAnosLetivos() {
        // ─── 2025 (encerrado) ───
        var al2025 = new AnoLetivo();
        al2025.setAno(2025);
        al2025.setDataInicio(LocalDate.of(2025, 2, 5));
        al2025.setDataEncerramento(LocalDate.of(2025, 12, 15));
        al2025.setDiasLetivos(200);
        al2025.setEncerrado(true);
        al2025.setFeriados(new ArrayList<>());
        al2025.setSemanasAvaliacao(new ArrayList<>());
        anoLetivoRepository.save(al2025);

        // ─── 2026 (ativo) ───
        var al2026 = new AnoLetivo();
        al2026.setAno(2026);
        al2026.setDataInicio(LocalDate.of(2026, 2, 4));
        al2026.setDataEncerramento(LocalDate.of(2026, 12, 14));
        al2026.setDiasLetivos(200);
        al2026.setEncerrado(false);
        
        // Feriados automáticos serão injetados pela nova funcionalidade no Service,
        // mas para o seed podemos deixar vazio ou apenas os base.
        al2026.setFeriados(new ArrayList<>());
        al2026.setSemanasAvaliacao(new ArrayList<>());
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

    private List<ModeloGrade> criarModelosGrade(List<Disciplina> disciplinas) {
        List<ModeloGrade> modelos = new ArrayList<>();
        // Disciplinas seed order: [0]Port, [1]Mat, [2]Cie, [3]His, [4]Geo, [5]EdFis, [6]Art, [7]Info
        Disciplina port = disciplinas.get(0);
        Disciplina mat  = disciplinas.get(1);
        Disciplina cie  = disciplinas.get(2);
        Disciplina his  = disciplinas.get(3);
        Disciplina geo  = disciplinas.get(4);
        Disciplina ef   = disciplinas.get(5);
        Disciplina art  = disciplinas.get(6);
        Disciplina inf  = disciplinas.get(7);

        java.time.DayOfWeek SEG = java.time.DayOfWeek.MONDAY;
        java.time.DayOfWeek TER = java.time.DayOfWeek.TUESDAY;
        java.time.DayOfWeek QUA = java.time.DayOfWeek.WEDNESDAY;
        java.time.DayOfWeek QUI = java.time.DayOfWeek.THURSDAY;
        java.time.DayOfWeek SEX = java.time.DayOfWeek.FRIDAY;

        // ── 1º ANO A ──
        var mg1a = criarModelo("Modelo A", 1);
        addD(mg1a, SEG, port, mat, art,  port, mat);
        addD(mg1a, TER, port, mat, ef,   geo,  port);
        addD(mg1a, QUA, mat,  port, inf,  mat,  port);
        addD(mg1a, QUI, port, mat, cie,  port, geo);
        addD(mg1a, SEX, mat,  port, his,  mat,  cie);
        modelos.add(modeloGradeRepository.save(mg1a));

        // ── 1º ANO B ──
        var mg1b = criarModelo("Modelo B", 1);
        addD(mg1b, SEG, port, mat, inf,  port, mat);
        addD(mg1b, TER, port, mat, cie,  geo,  port);
        addD(mg1b, QUA, mat,  port, his,  mat,  port);
        addD(mg1b, QUI, port, mat, art,  port, geo);
        addD(mg1b, SEX, mat,  port, ef,   mat,  cie);
        modelos.add(modeloGradeRepository.save(mg1b));

        // ── 2º ANO A ──
        var mg2a = criarModelo("Modelo A", 2);
        addD(mg2a, SEG, port, mat, port, cie,  mat);
        addD(mg2a, TER, art,  port, mat,  geo,  port);
        addD(mg2a, QUA, port, mat, ef,   port, cie);
        addD(mg2a, QUI, mat,  inf,  port, mat,  port);
        addD(mg2a, SEX, port, mat, his,  geo,  mat);
        modelos.add(modeloGradeRepository.save(mg2a));

        // ── 2º ANO B ──
        var mg2b = criarModelo("Modelo B", 2);
        addD(mg2b, SEG, port, mat, ef,   port, mat);
        addD(mg2b, TER, mat,  port, inf,  geo,  port);
        addD(mg2b, QUA, port, mat, cie,  port, cie);
        addD(mg2b, QUI, mat,  port, his,  mat,  port);
        addD(mg2b, SEX, art,  mat,  port, geo,  mat);
        modelos.add(modeloGradeRepository.save(mg2b));

        // ── 3º ANO A ──
        var mg3a = criarModelo("Modelo A", 3);
        addD(mg3a, SEG, port, mat, port, geo,  port);
        addD(mg3a, TER, mat,  port, cie,  mat,  geo);
        addD(mg3a, QUA, art,  mat,  port, his,  mat);
        addD(mg3a, QUI, port, ef,   mat,  port, cie);
        addD(mg3a, SEX, mat,  inf,  port, mat,  port);
        modelos.add(modeloGradeRepository.save(mg3a));

        // ── 3º ANO B ──
        var mg3b = criarModelo("Modelo B", 3);
        addD(mg3b, SEG, art,  ef,   inf,  port, mat);
        addD(mg3b, TER, port, mat, cie,  geo,  port);
        addD(mg3b, QUA, mat,  port, mat,  port, cie);
        addD(mg3b, QUI, port, mat, port, mat,  port);
        addD(mg3b, SEX, mat,  port, his,  geo,  mat);
        modelos.add(modeloGradeRepository.save(mg3b));

        // ── 4º ANO A ──
        var mg4a = criarModelo("Modelo A", 4);
        addD(mg4a, SEG, inf,  port, mat,  port, mat);
        addD(mg4a, TER, port, mat, cie,  geo,  port);
        addD(mg4a, QUA, mat,  port, his,  mat,  geo);
        addD(mg4a, QUI, art,  mat,  port, cie,  port);
        addD(mg4a, SEX, port, ef,   mat,  port, mat);
        modelos.add(modeloGradeRepository.save(mg4a));

        // ── 4º ANO B ──
        var mg4b = criarModelo("Modelo B", 4);
        addD(mg4b, SEG, port, mat, his,  port, mat);
        addD(mg4b, TER, art,  port, mat,  geo,  port);
        addD(mg4b, QUA, port, mat, ef,   port, cie);
        addD(mg4b, QUI, mat,  inf,  port, mat,  port);
        addD(mg4b, SEX, port, mat, cie,  geo,  mat);
        modelos.add(modeloGradeRepository.save(mg4b));

        // ── 5º ANO A ──
        var mg5a = criarModelo("Modelo A", 5);
        addD(mg5a, SEG, ef,   port, mat,  his,  port);
        addD(mg5a, TER, inf,  mat,  port, mat,  geo);
        addD(mg5a, QUA, port, mat, cie,  port, mat);
        addD(mg5a, QUI, mat,  port, mat,  cie,  port);
        addD(mg5a, SEX, art,  mat,  geo,  mat,  port);
        modelos.add(modeloGradeRepository.save(mg5a));

        // ── 5º ANO B ──
        var mg5b = criarModelo("Modelo B", 5);
        addD(mg5b, SEG, port, mat, port, geo,  mat);
        addD(mg5b, TER, mat,  port, cie,  mat,  port);
        addD(mg5b, QUA, art,  ef,   inf,  port, geo);
        addD(mg5b, QUI, port, mat, port, cie,  mat);
        addD(mg5b, SEX, mat,  port, his,  mat,  port);
        modelos.add(modeloGradeRepository.save(mg5b));

        log.info("✅ {} modelos de grade criados (grade exata do documento)", modelos.size());
        return modelos;
    }

    private ModeloGrade criarModelo(String nome, int serie) {
        var mg = new ModeloGrade();
        mg.setAnoLetivo(2026);
        mg.setSerie(serie);
        mg.setNome(nome);
        mg.setAulas(new ArrayList<>());
        return mg;
    }

    private void addD(ModeloGrade mg, java.time.DayOfWeek dia,
                      Disciplina a1, Disciplina a2, Disciplina a3, Disciplina a4, Disciplina a5) {
        addAula(mg, dia, 1, a1);
        addAula(mg, dia, 2, a2);
        addAula(mg, dia, 3, a3);
        addAula(mg, dia, 4, a4);
        addAula(mg, dia, 5, a5);
    }

    private void addAula(ModeloGrade mg, java.time.DayOfWeek dia, int num, Disciplina d) {
        var a = new ModeloGradeAula();
        a.setModeloGrade(mg);
        a.setDiaSemana(dia);
        a.setNumeroAula(num);
        a.setDisciplina(d);
        mg.getAulas().add(a);
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
            
            if (i >= 0 && i < 10) {
                p.setEspecialidade(com.poo.siga.model.enums.EspecialidadeProfessor.REGENTE);
            } else if (i == 10 || i == 11) {
                p.setEspecialidade(com.poo.siga.model.enums.EspecialidadeProfessor.ARTES);
            } else if (i == 12 || i == 13) {
                p.setEspecialidade(com.poo.siga.model.enums.EspecialidadeProfessor.EDUCACAO_FISICA);
            } else {
                p.setEspecialidade(com.poo.siga.model.enums.EspecialidadeProfessor.INFORMATICA);
            }
            
            lista.add(professorRepository.save(p));
        }
        log.info("✅ {} professores criados", lista.size());
        return lista;
    }

    private void criarUsuarios(List<Professor> professores) {
        // Senha padrão pública do seed — todos os usuários devem trocar no primeiro acesso.
        final String SENHA_PADRAO = "Siga2025@";
        String senhaPadrao = passwordEncoder.encode(SENHA_PADRAO);
        log.info("=== Seed: senha padrão inicial = '{}' (todos devem trocar no 1º acesso) ===", SENHA_PADRAO);

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
            u.setDataExpiracaoSenha(LocalDate.now().plusDays(1)); // expira rápido para forçar troca
            u.setPrimeiroAcesso(true); // força troca de senha no primeiro login
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
        u.setDataExpiracaoSenha(LocalDate.now().plusDays(1)); // expira rápido para forçar troca
        u.setPrimeiroAcesso(true); // força troca de senha no primeiro login
        return usuarioRepository.save(u);
    }

    private List<Turma> criarTurmas(List<Professor> professores, List<ModeloGrade> modelos, int anoLetivo) {
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
                
                // Assign a model to the turma
                int finalSerie = serie;
                var modelosSerie = modelos.stream().filter(m -> m.getSerie() == finalSerie).toList();
                if (!modelosSerie.isEmpty()) {
                    var modelo = modelosSerie.get(t % modelosSerie.size());
                    turma.setModeloGrade(modelo);
                }
                
                var savedTurma = turmaRepository.save(turma);
                turmas.add(savedTurma);
                
                // Criar grade horária baseada no modelo
                if (savedTurma.getModeloGrade() != null) {
                    var mapProfs = new java.util.HashMap<Integer, Professor>();
                    // Encontrar os professores específicos criados no seed
                    Professor profArtes = professores.stream().filter(p -> p.getEspecialidade() == com.poo.siga.model.enums.EspecialidadeProfessor.ARTES).findFirst().orElse(savedTurma.getProfessorRegente());
                    Professor profEdFis = professores.stream().filter(p -> p.getEspecialidade() == com.poo.siga.model.enums.EspecialidadeProfessor.EDUCACAO_FISICA).findFirst().orElse(savedTurma.getProfessorRegente());
                    Professor profInfo = professores.stream().filter(p -> p.getEspecialidade() == com.poo.siga.model.enums.EspecialidadeProfessor.INFORMATICA).findFirst().orElse(savedTurma.getProfessorRegente());
                    
                    // Associar as disciplinas específicas a esses professores. Os IDs são 6 (EdFis), 7 (Artes), 8 (Info) baseando na ordem de inserção do seed.
                    mapProfs.put(6, profEdFis);
                    mapProfs.put(7, profArtes);
                    mapProfs.put(8, profInfo);
                    
                    var discSet = new java.util.HashSet<Integer>();
                    for (var aula : savedTurma.getModeloGrade().getAulas()) {
                        var g = new GradeHoraria();
                        g.setTurma(savedTurma);
                        g.setDisciplina(aula.getDisciplina());
                        g.setDiaSemana(aula.getDiaSemana());
                        g.setNumeroAula(aula.getNumeroAula());
                        gradeHorariaRepository.save(g);
                        
                        if (!discSet.contains(aula.getDisciplina().getId())) {
                            discSet.add(aula.getDisciplina().getId());
                            var tdp = new TurmaDisciplinaProfessor();
                            tdp.setTurma(savedTurma);
                            tdp.setDisciplina(aula.getDisciplina());
                            tdp.setProfessor(mapProfs.getOrDefault(aula.getDisciplina().getId(), savedTurma.getProfessorRegente()));
                            tdpRepository.save(tdp);
                        }
                    }
                }
                
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
