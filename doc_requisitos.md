# DOCUMENTO DE REQUISITOS

## DO PROJETO:
**ESCOLA DE ENSINO FUNDAMENTAL HORIZONTE DO SABER**

*Mogi Mirim – SP*  
*2026*

---

# SUMÁRIO

1. Diretrizes de Projeto  
   1.1 Negócio do Usuário  
   1.2 O Propósito do Produto  

2. Cliente, Comprador e Outros Interessados  
   2.1 Cliente  
   2.2 Outros Interessados  

3. Usuários  

4. Restrições Necessárias  
   4.1 Restrições da Solução  
   4.2 Ambiente Atual da Implantação do Sistema  
   4.3 Restrições de Suporte  
   4.4 Restrições de Orçamento  

5. O Escopo do Trabalho  
   5.1 A Situação Atual  

6. O Escopo do Produto  
   6.1 Modelagem do Projeto  

---

# 1. DIRETRIZES DE PROJETO

## 1.1 NEGÓCIO DO USUÁRIO

A Escola de Ensino Fundamental Horizonte do Saber foi fundada em 2005 e funciona em uma única unidade desde então.

A escola possui um prédio com 4 pavimentos e atende nos períodos matutino e vespertino, de segunda a sexta-feira.

Atualmente a instituição possui aproximadamente **280 alunos matriculados** no Ensino Fundamental I (1º ao 5º ano) e **24 funcionários**, distribuídos entre equipe administrativa, professores e coordenação pedagógica.

Grande parte dos processos acadêmicos e administrativos ainda é realizada manualmente ou através de planilhas, como:

- controle de matrícula
- registro de notas
- frequência
- histórico escolar

## 1.2 O PROPÓSITO DO PRODUTO

Substituir o controle manual e baseado em planilhas por um **sistema informatizado de gestão escolar**.

O software centralizará processos críticos de secretaria e controle pedagógico, garantindo:

- integridade dos dados
- rastreabilidade
- aplicação automática das regras de negócio acadêmicas

---

# 2. CLIENTE, COMPRADOR E OUTROS INTERESSADOS

## 2.1 CLIENTE

**Prof. Carlos Henrique Almeida**  
Diretor Geral

## 2.2 OUTROS INTERESSADOS

- Equipe administrativa
- Secretaria
- Coordenação Pedagógica
- Professores
- Técnico de TI local

---

# 3. USUÁRIOS

Foram identificados três tipos de usuários do sistema:

- **Administrador:** acesso total ao sistema
- **Operador:** gerencia matrículas, alunos, históricos e documentos
- **Professor:** acesso restrito às próprias turmas

---

# 4. RESTRIÇÕES NECESSÁRIAS

## 4.1 RESTRIÇÕES DA SOLUÇÃO

O sistema deverá rodar em estações com **Windows 10**.

## 4.2 AMBIENTE ATUAL

- 12 computadores administrativos
- rede cabeada
- servidor dedicado Windows Server 2019
- 32GB RAM
- 8TB armazenamento

## 4.3 RESTRIÇÕES DE SUPORTE

A manutenção será suportada por **1 técnico de TI interno**.

## 4.4 RESTRIÇÕES DE ORÇAMENTO

Até o momento, não foram detectadas restrições.

---

# 5. O ESCOPO DO TRABALHO

## 5.1 A SITUAÇÃO ATUAL

### Infraestrutura

- 12 computadores administrativos  
  - Intel i5
  - 8GB RAM
  - HD 1TB

- 1 servidor dedicado  
  - Windows Server 2019
  - 32GB RAM
  - 8TB armazenamento

- rede local cabeada

---

## AUTENTICAÇÃO E SEGURANÇA

- tela de login como único ponto de entrada
- login por usuário e senha
- bloqueio após **5 tentativas inválidas**
- desbloqueio por administrador
- troca obrigatória de senha no primeiro acesso
- log de acesso com:
  - usuário
  - data
  - hora
  - operação
  - resultado
- encerramento automático após **30 minutos**
- senhas com hash criptográfico
- redefinição de senha por administrador

---

# MÓDULOS DO SISTEMA

## ALUNOS

Permitir:

- cadastro
- alteração
- inativação
- exclusão
- consulta

Campos obrigatórios:

- RA
- nome completo
- data de nascimento
- CPF
- e-mail
- responsável legal
- CPF do responsável

Regras:

- CPF válido e não duplicado
- aluno inativo não pode ser matriculado
- exclusão apenas sem vínculos acadêmicos

---

## PROFESSORES

Cadastro com:

- código funcional
- nome
- data nascimento
- CPF
- e-mail

Regras:

- CPF único
- inativação permitida
- exclusão apenas sem turmas vinculadas

---

## DISCIPLINAS

Campos:

- código
- descrição
- carga horária anual

---

## TURMAS

Campos:

- código
- ano letivo
- série
- identificação
- turno
- professor regente

Regras:

- não permitir duplicidade por combinação
- um aluno por turma/ano

---

## USUÁRIOS

Campos:

- código
- nome
- e-mail
- login
- senha inicial
- perfil
- data cadastro
- expiração senha

Perfis:

- Administrador
- Operador
- Professor

---

## MATRÍCULA ESCOLAR

Campos:

- número matrícula
- RA
- ano letivo
- série
- turma
- turno
- data
- situação

Situações:

- Ativa
- Trancada
- Cancelada

---

## NOTAS

Lançamento por:

- disciplina
- turma
- aluno
- bimestre
- tipo avaliação

Faixa:

- **0,0 a 10,0**

Cálculos automáticos:

- média bimestral
- média anual
- resultado final

---

## FREQUÊNCIA

Registro por:

- disciplina
- turma
- aluno
- data

Situação:

- presente
- ausente
- justificado

Cálculo automático de percentual.

---

## HISTÓRICO E BOLETIM

O sistema deve manter:

- anos letivos
- turmas
- disciplinas
- notas
- médias
- frequência
- resultado

---

## RELATÓRIOS

- alunos por turma
- frequência por período
- desempenho por disciplina
- risco de reprovação

Exportação:

- **PDF**
- impressão direta

---

# REGRAS DE NEGÓCIO

- nota mínima: **5,0**
- frequência mínima: **75%**
- 4 bimestres
- média anual = média das médias bimestrais
- reprovação por nota ou falta
- bloqueio após encerramento do ano letivo

---

# 6. O ESCOPO DO PRODUTO

## 6.1 MODELAGEM DO PROJETO

- Diagrama de Casos de Uso
- Diagrama de Classes
- DER
- Modelo Físico
- Mapeamento
- Innovation Canvas