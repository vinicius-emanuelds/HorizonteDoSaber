**ESCOLA DE ENSINO FUNDAMENTAL HORIZONTE DO SABER**

**Cores oficiais:** Azul, Branco e Verde

**Solicitante:** Prof. Carlos Henrique Almeida -- Diretor Geral

A Escola de Ensino Fundamental Horizonte do Saber foi fundada em 2005 e funciona em uma única unidade desde então. A escola possui um prédio com 4 pavimentos e atende nos períodos matutino e vespertino, de segunda a sexta-feira. Atualmente a instituição possui aproximadamente 280 alunos matriculados no Ensino Fundamental I (1º ao 5º ano) e 24 funcionários, distribuídos entre equipe administrativa, professores e coordenação pedagógica. Grande parte dos processos acadêmicos e administrativos ainda é realizada manualmente ou através de planilhas, como controle de matrícula, registro de notas, frequência e histórico escolar. A escola deseja desenvolver um **sistema informatizado de gestão escolar** para organizar seus processos acadêmicos e administrativos. A seguir um conjunto inicial de possíveis requisitos do sistema desejado:

1)  O sistema deverá operar na plataforma Windows 10, presente atualmente em todas as estações da escola. A escola possui um pequeno setor de TI com 1 técnico responsável pela manutenção dos computadores e da rede, que dará suporte à equipe de desenvolvimento.

2)  Infraestrutura atual da escola:

    a.  12 computadores administrativos (Intel i5, 8GB RAM, HD 1TB)

    b.  1 servidor dedicado com Windows Server 2019, com 32GB RAM e 8TB de armazenamento

    c.  Rede local via cabeamento estruturado

3)  Autenticação e Segurança:

    a.  O sistema deve exibir uma tela de login como único ponto de entrada.

    b.  O login deve ser feito por usuário (texto) e senha.

    c.  O sistema deve bloquear o acesso após 5 tentativas de login invalidas consecutivas para o mesmo usuário. O desbloqueio deve ser realizado por um Administrador.

    d.  O sistema deve exigir troca de senha no primeiro acesso ou quando a senha estiver expirada.

    e.  O sistema deve registrar log de acesso contendo: usuário, data, hora, tipo de operação e resultado (sucesso/falha).

    f.  O sistema deve encerrar a sessão automaticamente após 30 minutos de inatividade.

    g.  As senhas devem ser armazenadas com hash criptográfico. Nenhum perfil pode consultar a senha de outro usuário em texto claro.

    h.  O Administrador pode redefinir a senha de qualquer usuário, gerando uma senha temporária que expira no primeiro acesso.

4)  O sistema deve possuir 3 perfis de acesso:

    a.  ***Administrador***: possui acesso total ao sistema e suas funções.

    b.  ***Operador***: gerencia matrículas, cadastra e altera alunos, consulta históricos e imprime documentos.

    c.  ***Professor***: Acesso restrito a suas próprias turmas. Lança notas, frequência e consulta dados dos alunos vinculados à turma.

5)  ALUNOS

***Cadastro, alteração, inativação e exclusão: Administrador e Operador. Consulta: todos os perfis internos.***

a.  O sistema deve permitir cadastrar alunos com os campos obrigatórios: ***Registro*** ***Acadêmico*** (RA, gerado automaticamente e único), ***Nome*** ***completo***, ***Data*** ***de*** ***nascimento***, ***CPF***, ***E-mail***, ***Nome*** ***do*** ***responsável*** ***legal*** e ***CPF*** ***do*** ***responsável***.

b.  O sistema deve validar o CPF do aluno e do responsável no momento do cadastro, recusando valores inválidos ou duplicados.

c.  O sistema deve permitir alterar os dados cadastrais do aluno. Alterações devem ser registradas em log com data, hora e usuário responsável.

d.  O sistema deve permitir inativar um aluno. Aluno inativo não pode ser matriculado em novas turmas. Seu Histórico e preservado integralmente.

e.  O sistema deve permitir excluir um aluno somente se ele não possuir nenhuma matrícula ou Lançamento de notas/frequência. Caso contrário, a exclusão e bloqueada.

f.  O sistema deve permitir consultar alunos com filtros por nome, RA, CPF, situação (ativo/inativo) e turma

<!-- -->

6)  PROFESSORES

***Operações exclusivas do perfil Administrador.***

a.  O sistema deve permitir cadastrar professores com os campos: ***Código*** ***funcional*** (gerado automaticamente), ***Nome*** ***completo***, ***Data*** ***de*** ***nascimento***, ***CPF*** e ***E-mail***.

b.  O sistema deve validar o CPF do professor, recusando valores inválidos ou duplicados.

c.  O sistema deve permitir alterar os dados do professor.

d.  O sistema deve permitir inativar um professor. Professor inativo não pode ser associado a novas turmas. Turmas existentes com professor inativo devem ser sinalizadas para reatribuição.

e.  O sistema deve permitir excluir um professor somente se ele não estiver vinculado a nenhuma turma ativa ou Histórico de Lançamentos.

f.  O sistema deve permitir consultar professores com filtros por nome, código funcional e situação.

<!-- -->

7)  DISCIPLINAS

***Operações exclusivas do perfil Administrador.***

a.  O sistema deve permitir cadastrar disciplinas com os campos: ***Código*** (gerado automaticamente), ***Descrição*** e ***Carga*** ***horária*** ***anual*** (em horas-aula).

b.  O sistema deve permitir alterar e inativar disciplinas.

c.  O sistema deve permitir excluir uma disciplina somente se ela não estiver vinculada a nenhuma turma ativa ou Histórico acadêmico.

d.  O sistema deve permitir consultar disciplinas com filtros por código, descrição e situação.

<!-- -->

8)  TURMAS

***Operações exclusivas do perfil Administrador.***

a.  O sistema deve permitir cadastrar turmas com os campos: ***Código*** (gerado automaticamente), ***Ano*** ***letivo***, ***Serie*** (1 ao 5 ano), ***Nome***/***identificação*** ***da*** ***turma*** (ex: 3A), ***Turno*** (Matutino / Vespertino) e ***Professor*** ***regente***.

b.  Cada turma pode ter disciplinas especificas atribuídas a professores diferentes do regente, para os casos de Informática, Educação Física e Artes.

c.  O sistema deve impedir o cadastro de duas turmas com a mesma combinação de Ano letivo + Serie + Nome + Turno.

d.  O sistema deve permitir vincular alunos a uma turma. Um aluno só pode estar ativo em uma turma por ano letivo.

e.  O sistema deve permitir alterar e inativar turmas.

f.  O sistema deve permitir excluir uma turma somente se ela não possuir alunos vinculados ou Lançamentos de notas e frequência.

<!-- -->

9)  USUÁRIOS

***Operações exclusivas do perfil Administrador.***

a.  O sistema deve permitir cadastrar usuários com os campos: ***Código*** (gerado automaticamente), ***Nome*** ***completo***, ***E-mail***, ***Nome*** ***de*** ***usuário*** (login), ***Senha*** ***inicial***, ***Perfil*** ***de*** ***acesso*** (Administrador / Operador / Professor), ***Data*** ***de*** ***cadastro*** (gerada automaticamente) e ***Data*** ***de*** ***expiração*** ***da*** ***senha***.

b.  O sistema deve permitir alterar todos os dados do usuário, exceto o código e a data de cadastro.

c.  O sistema deve permitir inativar um usuário. Usuário inativo não pode acessar o sistema. Seu Histórico de operações e preservado.

d.  O sistema deve permitir excluir um usuário somente se ele não possuir nenhuma operação registrada no sistema. Caso contrário, a exclusão e bloqueada e o sistema deve exibir mensagem explicativa.

e.  O sistema deve permitir consultar a lista de usuários com filtros por nome, perfil e situação (ativo/inativo).

f.  Quando o perfil for Professor, o campo Professor do cadastro de usuários deve estar vinculado ao registro de professor existente no modulo de professores

<!-- -->

10) MATRÍCULA ESCOLAR

***Operações realizadas por Administrador e Operador.***

a.  O sistema deve manter um formulário de matrícula vinculado ao cadastro do aluno, com os campos: ***Número*** ***da*** ***matrícula*** (gerado automaticamente), ***RA*** ***do*** ***aluno***, ***Ano*** ***letivo***, ***Serie***, ***Turma***, ***Turno***, ***Data*** ***da*** ***matrícula*** e ***Situação*** (Ativa / Trancada / Cancelada).

b.  Um aluno ativo pode ter apenas uma matrícula ativa por ano letivo.

c.  O sistema deve permitir alterar a situação da matrícula. Ao trancar ou cancelar, deve ser registrado o motivo e a data da ocorrência.

d.  Uma matrícula cancelada não pode ser reativada. Deve ser criada uma matrícula caso o aluno retorne.

e.  O sistema deve registrar o Histórico de todas as matrículas do aluno, incluindo as canceladas e trancadas.

f.  Notas e frequências lançadas antes do cancelamento da matrícula devem ser preservadas no Histórico.

<!-- -->

11) NOTAS

***Professor lançar notas das próprias turmas. Administrador pode lançar e alterar notas de qualquer turma.***

a.  O sistema deve permitir o Lançamento de notas por ***Disciplina***, ***Turma***, ***Aluno***, ***Período*** ***letivo*** (1 ao 4 bimestre) e ***Tipo*** ***de*** ***avaliação*** (Avaliação 1, Avaliação 2, Recuperação).

b.  Notas devem ser numéricas, no intervalo de 0,0 a 10,0, com uma casa decimal.

c.  O sistema deve calcular automaticamente a média bimestral com base nas avaliações lançadas, conforme a regra de cálculo configurada (ver Regras de Negócio).

d.  O sistema deve calcular automaticamente a média anual e o resultado (Aprovado / Reprovado / Em recuperação) ao fim do ano letivo.

e.  O sistema deve permitir corrigir um Lançamento de nota, registrando o valor anterior, o novo valor, a data da correção e o usuário responsável.

f.  O sistema deve alertar o usuário ao tentar lançar nota para um aluno com matrícula inativa ou cancelada.

g.  O sistema deve exibir o quadro de notas da turma, mostrando todos os alunos e suas notas por período e disciplina.

<!-- -->

12) FREQUÊNCIAS

***Professor lançar frequência das próprias turmas. Administrador pode lançar e alterar frequência de qualquer turma.***

a.  O sistema deve permitir o Lançamento de frequência por Disciplina, Turma, Aluno e Data.

b.  O registro de frequência deve indicar: Presente, ausente ou justificado.

c.  O sistema deve calcular automaticamente o percentual de presença do aluno por disciplina e no geral.

d.  O sistema deve sinalizar alunos com frequência abaixo do mínimo exigido (ver Regras de Negócio).

e.  O sistema deve permitir registrar a justificativa de falta, associada a data e ao aluno.

f.  O sistema deve exibir o diário de classe com a lista de alunos e o registro de presença por data.

<!-- -->

13) HISTÓRICO E BOLETIM

***Operações realizadas por Administrador e Operador.***

a.  O sistema deve manter o Histórico escolar completo do aluno, contendo: todos os anos letivos, turmas, disciplinas, notas por período, média final, frequência e resultado (Aprovado / Reprovado).

b.  O Histórico deve poder ser impresso em formato padrão, com os dados da escola, assinatura do responsável e data de emissão.

c.  O boletim deve consolidar as notas e a frequência do aluno por período letivo e disciplina.

<!-- -->

14) RELATÓRIOS

***Operações realizadas por Administrador e Operador.***

a.  Relatório de alunos por turma: lista todos os alunos de uma turma com situação da matrícula.

b.  Relatório de frequência por turma/período: exibe o percentual de presença de cada aluno, com destaque para os que estão abaixo do mínimo.

c.  Relatório de desempenho por disciplina: exibe média da turma por disciplina em cada período.

d.  Relatório de alunos em risco de reprovação: lista alunos com média abaixo do mínimo ou frequência insuficiente.

e.  Todos os relatórios devem ser exportados para PDF e imprimíveis diretamente pelo sistema

<!-- -->

15) O sistema deve possuir um cadastro de Ano Letivo contendo: data de início, data de encerramento, dias letivos, feriados e datas de avaliação. Essas informações são de caráter informativo e de consulta. Ao atingir a data de encerramento, o sistema bloqueia automaticamente novos lançamentos de nota e frequência para todas as turmas daquele ano. Somente o Administrador pode cadastrar e alterar o Ano Letivo.

16) Regras de Negócio:

    a.  A nota mínima para aprovação em cada disciplina e 5,0 (cinco).

    b.  O ano letivo e dividido em 4 bimestres. Cada bimestre possui duas avaliações (AV1 e AV2) e uma recuperação bimestral (REC).

    c.  A média bimestral e calculada como: se (AV1 + AV2) / 2 \>= 5,0, então a média e (AV1 + AV2) / 2. Caso contrário, se REC foi aplicada, a média final do bimestre e a maior nota entre a média das duas avaliações e a nota da recuperação.

    d.  A média anual e a média aritmética simples das quatro medias bimestrais.

    e.  O aluno e considerado aprovado se a média anual for \>= 5,0 em todas as disciplinas E a frequência anual for \>= 75% da carga horaria.

    f.  O aluno e considerado reprovado por nota se a média anual em qualquer disciplina for \< 5,0 após a recuperação final.

    g.  O aluno e considerado reprovado por falta se a frequência anual for \< 75% da carga horaria, independentemente das notas.

    h.  Notas Não lançadas até o encerramento do período devem ser sinalizadas como pendentes no sistema.

    i.  A frequência mínima exigida e de 75% da carga horaria total anual.

    j.  O registro de frequência e feito por aula, não por dia. Uma aula dupla na mesma disciplina gera dois registros.

    k.  Faltas justificadas são contabilizadas no total de faltas para efeito de cálculo de frequência, mas podem ser consideradas pela coordenação para análise de casos especiais.

    l.  O sistema deve emitir alerta automático quando o aluno atingir 50% do limite de faltas permitido.

    m.  Um aluno só pode estar matriculado em uma turma ativa por ano letivo.

    n.  Um aluno inativo não pode ser matriculado em nenhuma turma.

    o.  A exclusão física de uma matrícula e proibida. A operação de cancelamento e definitiva, mas preserva o registro.

    p.  Cada turma tem um professor regente responsável pela maioria das disciplinas.

    q.  As disciplinas Informática, Educação Física e Artes podem ser atribuídas a professores diferentes do regente.

    r.  O encerramento de um ano letivo deve bloquear novos Lançamentos de nota e frequência para as turmas daquele ano.
