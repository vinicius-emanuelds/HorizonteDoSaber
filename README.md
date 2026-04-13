# SIGA — Sistema Integrado de Gestão Acadêmica

Projeto desenvolvido para a disciplina Laboratório de Engenharia de Software, do curso de Análise e Desenvolvimento de Sistemas da FATEC Mogi Mirim (2026), tendo como "cliente fictício" a Escola de Ensino Fundamental Horizonte do Saber.

O sistema foi criado para substituir o controle manual feito em planilhas. Hoje a escola gerencia matrículas, notas, frequência e histórico escolar de forma descentralizada. O SIGA centraliza tudo isso em uma aplicação web acessada pelo navegador, dentro da própria rede da escola.

---

## O que o sistema faz

- Controle de login com três níveis de acesso: Administrador, Operador e Professor
- Cadastro de alunos, professores, turmas e disciplinas
- Registro de matrículas com histórico preservado mesmo após cancelamentos
- Lançamento de notas por bimestre com cálculo automático de médias
- Registro de frequência por aula com alerta quando o aluno se aproxima do limite de faltas
- Emissão de boletim e histórico escolar com exportação em PDF
- Relatórios de desempenho, frequência e alunos em risco de reprovação

---

## Tecnologias usadas

| Parte | Tecnologia |
|---|---|
| Backend | Java 21 + Spring Boot 3.5.7 |
| Autenticação | Spring Security + JWT |
| Banco de dados | PostgreSQL 16 |
| Frontend | Thymeleaf + Bootstrap 5 + HTML/CSS/JS |
| Ambiente local | Docker + docker-compose |
| Build | Maven |

O sistema roda no servidor dedicado da escola (Windows Server 2019) e é acessado pelos computadores administrativos via rede local, sem precisar instalar nada nas máquinas dos funcionários.

---

## Como rodar o projeto localmente

**Pré-requisitos:** Java 21, Maven e Docker instalados.

```bash
# 1. Clone o repositório
git clone https://github.com/vinicius-emanuelds/HorizonteDoSaber.git
cd HorizonteDoSaber

# 2. Suba o banco de dados com Docker
docker-compose up -d
```

O sistema vai estar disponível em `http://localhost:8080`.

A documentação dos endpoints fica em `http://localhost:8080/swagger-ui.html`.

---

## Estrutura do projeto

```
src/
├── main/
│   ├── java/com/poo/siga/
│   │   ├── controller/    # Recebe as requisições e direciona para o serviço certo
│   │   ├── service/       # Regras de negócio (cálculo de médias, validações, etc.)
│   │   ├── repository/    # Acesso ao banco de dados
│   │   ├── model/         # Entidades: Aluno, Professor, Turma, Nota, etc.
│   │   ├── dto/           # Objetos de transferência de dados entre camadas
│   │   └── security/      # Configuração de login, perfis e token JWT
│   └── resources/
│       ├── templates/     # Páginas HTML (Thymeleaf)
│       └── static/        # CSS, JS e imagens
└── test/                  # Testes unitários e de integração
```

---

## Organização do desenvolvimento

- A branch `main` só recebe código após revisão de outro membro do time
- Mensagens de commit descrevem o que foi feito — ex: `adiciona validacao de CPF no cadastro de alunos`

---

## Time

| Nome | Papel |
|---|---|
| Vinicius Silva | Manager / Backend — lógica do sistema, autenticação e deploy |
| Marcelo Belloto | Frontend / UX — telas, integração e geração de relatórios |
| Lucas Vieira | Fullstack / DBA — banco de dados, consultas e suporte geral |

---
