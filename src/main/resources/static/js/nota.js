//JS da nota

// Suporte para vir redirecionado do turma-detalhe.html
const urlParams = new URLSearchParams(window.location.search);
const initialTurmaId = urlParams.get('turma');

document.addEventListener('DOMContentLoaded', () => {
    if (!localStorage.getItem('token')) { window.location.href='/login'; return; }
    carregarDrops();
});

async function carregarDrops() {
    const [turmas, disc] = await Promise.all([
        (await apiFetch('/api/turmas?size=100&ativo=true')).json(),
        (await apiFetch('/api/disciplinas?size=100&ativo=true')).json()
    ]);
    
    document.getElementById('filtroTurma').innerHTML = '<option value="">Selecione a turma</option>' + 
        (turmas.content||[]).map(t => `<option value="${t.id}">Turma ${t.serie}º ${t.nome}</option>`).join('');
    document.getElementById('filtroDisciplina').innerHTML = '<option value="">Selecione a disciplina</option>' + 
        (disc.content||[]).map(d => `<option value="${d.id}">${d.descricao}</option>`).join('');

    if (initialTurmaId) {
        document.getElementById('filtroTurma').value = initialTurmaId;
    }
}

async function carregarNotas() {
    const turmaId = document.getElementById('filtroTurma').value;
    const disciplinaId = document.getElementById('filtroDisciplina').value;
    const periodo = document.getElementById('filtroPeriodo').value;
    const container = document.getElementById('notasContainer');

    if (!turmaId || !disciplinaId || !periodo) {
        container.innerHTML = '<div class="empty-state"><i class="bi bi-clipboard-data d-block"></i><p>Selecione Turma, Disciplina e Bimestre</p></div>';
        return;
    }

    container.innerHTML = '<div class="text-center py-4"><div class="spinner-border text-primary"></div></div>';

    try {
        // Busca alunos matriculados na turma e notas do período específico
        const [alunosMatriculados, notasPeriodo] = await Promise.all([
            (await apiFetch(`/api/matriculas/turma/${turmaId}`)).json(),
            (await apiFetch(`/api/notas/turma/${turmaId}/disciplina/${disciplinaId}/periodo/${periodo}`)).json()
        ]);

        if (!alunosMatriculados.length) {
            container.innerHTML = '<div class="empty-state"><p>Nenhum aluno matriculado nesta turma</p></div>';
            return;
        }

        // Monta mapa: alunoId -> { AV1: nota, AV2: nota, REC: nota }
        const mapNotas = {};
        notasPeriodo.forEach(n => {
            if (!mapNotas[n.alunoId]) mapNotas[n.alunoId] = {};
            mapNotas[n.alunoId][n.tipoAvaliacao] = n;
        });

        const periodoLabel = `${periodo}º Bimestre`;

        let html = `<div class="table-responsive"><table class="table table-hover align-middle">
            <thead class="table-light text-center"><tr>
                <th class="text-start">Aluno</th>
                <th width="130">AV1 — ${periodoLabel}</th>
                <th width="130">AV2 — ${periodoLabel}</th>
                <th width="130">Recuperação</th>
                <th width="110">Média Bim.</th>
                <th>Status</th>
            </tr></thead><tbody>`;

        alunosMatriculados.forEach(m => {
            const stuId = m.alunoId;
            const n = mapNotas[stuId] || {};

            const av1 = n.AV1 ? n.AV1.valor : '';  const idAv1 = n.AV1 ? n.AV1.id : '';
            const av2 = n.AV2 ? n.AV2.valor : '';  const idAv2 = n.AV2 ? n.AV2.id : '';
            const rec = n.REC ? n.REC.valor : '';   const idRec = n.REC ? n.REC.id : '';

            // Calcula média bimestral localmente para feedback imediato
            let mediaHtml = '-';
            let aprovado = false;
            if (av1 !== '' && av2 !== '') {
                let media = (parseFloat(av1) + parseFloat(av2)) / 2;
                if (media < 5 && rec !== '') media = Math.max(media, parseFloat(rec));
                aprovado = media >= 5;
                mediaHtml = media.toFixed(1);
            }

            const statusHtml = mediaHtml === '-'
                ? '<span class="badge bg-secondary">Pendente</span>'
                : aprovado
                    ? '<span class="badge bg-success">Aprovado</span>'
                    : '<span class="badge bg-danger">Rec/Reprovado</span>';

            html += `<tr>
                <td class="fw-bold text-start"><small class="text-muted d-block">${m.alunoRa}</small>${m.alunoNome}</td>
                <td><input type="number" step="0.1" min="0" max="10" class="form-control form-control-sm text-center" value="${av1}"
                     onblur="salvarNota(this, ${stuId}, ${periodo}, 'AV1', '${idAv1}')"></td>
                <td><input type="number" step="0.1" min="0" max="10" class="form-control form-control-sm text-center" value="${av2}"
                     onblur="salvarNota(this, ${stuId}, ${periodo}, 'AV2', '${idAv2}')"></td>
                <td class="bg-light border-start"><input type="number" step="0.1" min="0" max="10" class="form-control form-control-sm text-center border-warning" value="${rec}"
                     onblur="salvarNota(this, ${stuId}, ${periodo}, 'REC', '${idRec}')" placeholder="-"></td>
                <td class="text-center"><strong class="${aprovado ? 'text-success' : (mediaHtml !== '-' ? 'text-danger' : '')}">${mediaHtml}</strong></td>
                <td class="text-center">${statusHtml}</td>
            </tr>`;
        });

        html += `</tbody></table></div>
        <div class="alert alert-info py-2 mt-3 mb-0" style="font-size: 13px">
            <i class="bi bi-info-circle me-2"></i>
            Digite a nota do aluno no campo correspondente (AV1 ou AV2) e clique fora para salvar automaticamente.
            A Recuperação só é considerada quando a média das AV1+AV2 for inferior a 5,0.
        </div>`;
        container.innerHTML = html;

    } catch (e) {
        console.error(e);
        if (e.message && e.message.includes('expirada')) return;
        container.innerHTML = `<div class="alert alert-danger">Erro ao carregar dados.</div>`;
    }
}

async function salvarNota(inputNode, alunoId, periodoInt, tipoAvaliacao, notaIdOriginal) {
    const valor = inputNode.value;
    if (valor === '') return;
    
    let num = parseFloat(valor);
    if (num < 0) num = 0;
    if (num > 10) num = 10;
    inputNode.value = num.toFixed(1);

    const turmaId = document.getElementById('filtroTurma').value;
    const disciplinaId = document.getElementById('filtroDisciplina').value;

    inputNode.style.borderColor = 'orange';

    try {
        if (notaIdOriginal && notaIdOriginal !== 'undefined' && notaIdOriginal !== '') {
            // Atualização via PUT — ID já conhecido
            await apiFetch(`/api/notas/${notaIdOriginal}`, {
                method: 'PUT', body: JSON.stringify({ valor: num })
            });
            inputNode.style.borderColor = '#27ae60';
            inputNode.style.backgroundColor = '#eafaf1';
            setTimeout(() => { inputNode.style.borderColor = ''; inputNode.style.backgroundColor = ''; }, 800);
        } else {
            // Novo lançamento via POST
            const res = await apiFetch('/api/notas', {
                method: 'POST', body: JSON.stringify({
                    turmaId: parseInt(turmaId),
                    disciplinaId: parseInt(disciplinaId),
                    alunoId: alunoId,
                    periodo: periodoInt,
                    tipoAvaliacao: tipoAvaliacao,  // AV1, AV2 ou REC — valores válidos do enum
                    valor: num
                })
            });
            const novaNota = res.ok ? await res.json() : null;
            if (novaNota && novaNota.id) {
                // Atualiza o onblur para usar PUT nas próximas edições
                inputNode.setAttribute('onblur',
                    `salvarNota(this, ${alunoId}, ${periodoInt}, '${tipoAvaliacao}', '${novaNota.id}')`);
            }
            inputNode.style.borderColor = '#27ae60';
            inputNode.style.backgroundColor = '#eafaf1';
            setTimeout(() => { inputNode.style.borderColor = ''; inputNode.style.backgroundColor = ''; }, 800);
        }

        atualizarMediaLinha(inputNode);

    } catch (e) {
        if (e.message && e.message.includes('expirada')) return;
        inputNode.style.borderColor = 'red';
        console.error(e);
        alert('Falha ao salvar nota.');
    }
}

/**
 * Recalcula Média Bimestral e Status da linha sem recarregar a tabela.
 * Ordem das colunas de input: AV1, AV2, REC
 */
function atualizarMediaLinha(inputNode) {
    const tr = inputNode.closest('tr');
    if (!tr) return;

    const inputs = tr.querySelectorAll('input[type="number"]');
    const vals = Array.from(inputs).map(i => i.value !== '' ? parseFloat(i.value) : null);
    const [av1, av2, rec] = vals;

    const tdMedia = tr.cells[tr.cells.length - 2];
    const tdStatus = tr.cells[tr.cells.length - 1];
    if (!tdMedia || !tdStatus) return;

    if (av1 !== null && av2 !== null) {
        let media = (av1 + av2) / 2;
        if (media < 5 && rec !== null) media = Math.max(media, rec);
        const aprovado = media >= 5;
        tdMedia.innerHTML = `<strong class="${aprovado ? 'text-success' : 'text-danger'}">${media.toFixed(1)}</strong>`;
        tdStatus.innerHTML = aprovado
            ? '<span class="badge bg-success">Aprovado</span>'
            : '<span class="badge bg-danger">Rec/Reprovado</span>';
    } else {
        tdMedia.innerHTML = '<strong>-</strong>';
        tdStatus.innerHTML = '<span class="badge bg-secondary">Pendente</span>';
    }
}
