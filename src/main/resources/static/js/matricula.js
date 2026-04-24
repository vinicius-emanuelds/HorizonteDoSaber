//JS da matricula
const API = '/api/matriculas';

let _buscarTimeout = null;

document.addEventListener('DOMContentLoaded', () => {
    if (!localStorage.getItem('token')) { window.location.href = '/login'; return; }
    buscar();
    carregarSelects();
});

// Mapa de badge e label para cada situação
const SIT_META = {
    ATIVA:     { css: 'ativo',     label: 'Ativa' },
    TRANCADA:  { css: 'trancada',  label: 'Trancada' },
    CANCELADA: { css: 'cancelada', label: 'Cancelada' },
    CONCLUIDA: { css: 'concluida', label: 'Concluída' }
};

// ─── Filtros ──────────────────────────────────────────────────────────────────

function buscarComDelay() {
    clearTimeout(_buscarTimeout);
    _buscarTimeout = setTimeout(buscar, 400);
}

function limparFiltros() {
    document.getElementById('filtroNome').value = '';
    document.getElementById('filtroTurma').value = '';
    document.getElementById('filtroAno').value = '2026';
    document.getElementById('filtroSituacao').value = '';
    buscar();
}

async function buscar() {
    const nome = document.getElementById('filtroNome').value;
    const turmaId = document.getElementById('filtroTurma').value;
    const ano = document.getElementById('filtroAno').value;
    const situacao = document.getElementById('filtroSituacao').value;

    let url = `${API}?size=200`;
    if (nome) url += `&nomeAluno=${encodeURIComponent(nome)}`;
    if (turmaId) url += `&turmaId=${turmaId}`;
    if (ano) url += `&anoLetivo=${ano}`;
    if (situacao) url += `&situacao=${situacao}`;

    try {
        const data = await (await apiFetch(url)).json();
        renderizar(data.content || []);
    } catch (e) { console.error(e); }
}

function renderizar(list) {
    const tb = document.getElementById('lista');
    if (!list.length) {
        tb.innerHTML = '<tr><td colspan="7"><div class="empty-state"><p>Nenhuma matrícula encontrada com esses filtros</p></div></td></tr>';
        return;
    }

    tb.innerHTML = list.map(m => {
        const meta = SIT_META[m.situacao] || { css: '', label: m.situacao };

        let botoes = '';
        if (m.situacao === 'ATIVA') {
            botoes = `
                <button class="btn-action" style="color:#f39c12"
                    onclick="abrirConfirmacao(${m.id},'trancar','Trancar Matrícula')"
                    title="Trancar"><i class="bi bi-pause-circle"></i></button>
                <button class="btn-action" style="color:#e74c3c"
                    onclick="abrirConfirmacao(${m.id},'cancelar','Cancelar Matrícula')"
                    title="Cancelar"><i class="bi bi-x-circle"></i></button>
                <button class="btn-action" style="color:#27ae60"
                    onclick="confirmarConcluir(${m.id})"
                    title="Concluir"><i class="bi bi-check-circle"></i></button>`;
        } else if (m.situacao === 'TRANCADA') {
            botoes = `
                <button class="btn-action" style="color:#3498db"
                    onclick="reativar(${m.id})"
                    title="Reativar"><i class="bi bi-play-circle"></i></button>
                <button class="btn-action" style="color:#e74c3c"
                    onclick="abrirConfirmacao(${m.id},'cancelar','Cancelar Matrícula')"
                    title="Cancelar"><i class="bi bi-x-circle"></i></button>
                <button class="btn-action" style="color:#27ae60"
                    onclick="confirmarConcluir(${m.id})"
                    title="Concluir"><i class="bi bi-check-circle"></i></button>`;
        } else if (m.situacao === 'CONCLUIDA') {
            botoes = `
                <button class="btn-action" style="color:#3498db"
                    onclick="reativar(${m.id})"
                    title="Reativar por engano"><i class="bi bi-play-circle"></i></button>`;
        }

        return `<tr>
            <td><strong>${m.numero}</strong></td>
            <td>${m.alunoNome}</td>
            <td>${m.alunoRa}</td>
            <td>${m.turmaIdentificacao}</td>
            <td>${m.anoLetivo}</td>
            <td><span class="badge-status ${meta.css}">${meta.label}</span></td>
            <td class="d-flex gap-1">${botoes}</td>
        </tr>`;
    }).join('');
}

async function carregarSelects() {
    const alunos = await (await apiFetch('/api/alunos?size=500&ativo=true')).json();
    const turmas = await (await apiFetch('/api/turmas?size=200')).json();

    document.getElementById('matAluno').innerHTML =
        '<option value="">Selecione...</option>' +
        (alunos.content || []).map(a => `<option value="${a.id}">${a.ra} - ${a.nome}</option>`).join('');
    document.getElementById('matTurma').innerHTML =
        '<option value="">Selecione...</option>' +
        (turmas.content || []).map(t => `<option value="${t.id}">${t.identificacao} / ${t.anoLetivo}</option>`).join('');

    // Filtro de turma na barra
    document.getElementById('filtroTurma').innerHTML =
        '<option value="">Todas as turmas</option>' +
        (turmas.content || []).map(t => `<option value="${t.id}">${t.identificacao} / ${t.anoLetivo}</option>`).join('');

    // Selects do modal de rematrícula
    const opts = (turmas.content || []).map(t => `<option value="${t.id}">${t.identificacao} / ${t.anoLetivo}</option>`).join('');
    document.getElementById('remOrigem').innerHTML = '<option value="">Selecione a turma de origem</option>' + opts;
    document.getElementById('remDestino').innerHTML = '<option value="">Selecione a turma de destino</option>' + opts;
}

function abrirModal() {
    new bootstrap.Modal(document.getElementById('modalMatricula')).show();
}

async function salvar() {
    const body = {
        alunoId: parseInt(document.getElementById('matAluno').value),
        turmaId: parseInt(document.getElementById('matTurma').value)
    };
    if (!body.alunoId || !body.turmaId) { alert('Selecione o aluno e a turma.'); return; }
    const res = await apiFetch(API, { method: 'POST', body: JSON.stringify(body) });
    if (!res.ok) { alert(await extrairMensagem(res)); return; }
    bootstrap.Modal.getInstance(document.getElementById('modalMatricula')).hide();
    buscar();
}

// ─── Reativar ────────────────────────────────────────────────────────────────

async function reativar(id) {
    if (!confirm('Reativar esta matrícula? A situação voltará para ATIVA.')) return;
    try {
        const res = await apiFetch(`${API}/${id}/reativar`, { method: 'PATCH' });
        if (!res.ok) { alert(await extrairMensagem(res)); return; }
        buscar();
    } catch (e) {
        if (e.message && e.message.includes('expirada')) return;
        alert('Erro de rede ao reativar.');
    }
}

// ─── Modal de confirmação com motivo ─────────────────────────────────────────

let _acaoAtual = null;

function abrirConfirmacao(id, action, title) {
    _acaoAtual = { id, action, title };
    document.getElementById('confirmacaoTitulo').textContent = title;
    document.getElementById('confirmacaoMotivo').value = '';
    new bootstrap.Modal(document.getElementById('modalConfirmacao')).show();
}

async function executarAcao() {
    if (!_acaoAtual) return;
    const { id, action } = _acaoAtual;
    const motivo = document.getElementById('confirmacaoMotivo').value.trim()
                   || (action + ' solicitado');

    const url = `${API}/${id}/${action}`;
    const btnConfirmar = document.getElementById('btnConfirmarAcao');
    btnConfirmar.disabled = true;
    btnConfirmar.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Salvando...';

    try {
        const res = await apiFetch(url, { method: 'PATCH', body: JSON.stringify({ motivo }) });
        if (!res.ok) { alert('Erro: ' + await extrairMensagem(res)); return; }
        const modalEl = document.getElementById('modalConfirmacao');
        const bsModal = bootstrap.Modal.getInstance(modalEl);
        if (bsModal) bsModal.hide();
        buscar();
    } catch (e) {
        if (e.message && e.message.includes('expirada')) return;
        alert('Erro de rede ao executar a ação.');
    } finally {
        btnConfirmar.disabled = false;
        btnConfirmar.innerHTML = '<i class="bi bi-check-lg me-1"></i>Confirmar';
    }
}

async function confirmarConcluir(id) {
    if (!confirm('Concluir esta matrícula? Esta ação indica que o aluno finalizou o ano letivo.')) return;
    try {
        const res = await apiFetch(`${API}/${id}/concluir`, { method: 'PATCH', body: JSON.stringify({}) });
        if (!res.ok) { alert(await extrairMensagem(res)); return; }
        buscar();
    } catch (e) {
        if (e.message && e.message.includes('expirada')) return;
        alert('Erro de rede ao concluir.');
    }
}

// ─── Rematrícula em Lote ──────────────────────────────────────────────────────

function abrirModalRematricula() {
    document.getElementById('remResultado').classList.add('d-none');
    document.getElementById('remResultado').innerHTML = '';
    new bootstrap.Modal(document.getElementById('modalRematricula')).show();
}

async function executarRematricula() {
    const turmaOrigemId = parseInt(document.getElementById('remOrigem').value);
    const turmaDestinoId = parseInt(document.getElementById('remDestino').value);

    if (!turmaOrigemId || !turmaDestinoId) {
        alert('Selecione as duas turmas para continuar.'); return;
    }
    if (turmaOrigemId === turmaDestinoId) {
        alert('A turma de origem e destino não podem ser iguais.'); return;
    }

    const btn = document.getElementById('btnExecutarRematricula');
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Processando...';

    try {
        const res = await apiFetch(`${API}/rematricula`, {
            method: 'POST',
            body: JSON.stringify({ turmaOrigemId, turmaDestinoId })
        });
        const resultado = await res.json();

        if (!res.ok) {
            document.getElementById('remResultado').innerHTML =
                `<div class="alert alert-danger">${resultado.erro || 'Erro ao executar rematrícula.'}</div>`;
        } else {
            const ignoradosList = resultado.ignoradosNomes && resultado.ignoradosNomes.length
                ? `<ul class="mb-0 mt-2">${resultado.ignoradosNomes.map(n => `<li>${n}</li>`).join('')}</ul>`
                : '';
            document.getElementById('remResultado').innerHTML = `
                <div class="alert alert-success">
                    <strong><i class="bi bi-check-circle me-1"></i>Rematrícula concluída!</strong><br>
                    Total de alunos: <strong>${resultado.total}</strong> |
                    Matriculados: <strong class="text-success">${resultado.matriculados}</strong> |
                    Ignorados: <strong class="text-warning">${resultado.ignorados}</strong>
                    ${ignoradosList ? `<details class="mt-2"><summary>Ver alunos ignorados</summary>${ignoradosList}</details>` : ''}
                </div>`;
            buscar();
        }
        document.getElementById('remResultado').classList.remove('d-none');
    } catch (e) {
        if (e.message && e.message.includes('expirada')) return;
        alert('Erro de rede ao executar a rematrícula.');
    } finally {
        btn.disabled = false;
        btn.innerHTML = '<i class="bi bi-arrow-repeat me-1"></i>Executar Rematrícula';
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

async function extrairMensagem(res) {
    try {
        const data = await res.json();
        return data.erro || data.message || data.error || JSON.stringify(data);
    } catch (_) {
        return `HTTP ${res.status} ${res.statusText}`;
    }
}
