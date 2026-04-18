//JS da matricula
const API = '/api/matriculas';
const h = () => ({ 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + localStorage.getItem('token') });

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

async function buscar() {
    const data = await (await fetch(`${API}?size=100`, { headers: h() })).json();
    renderizar(data.content || []);
}

function renderizar(list) {
    const tb = document.getElementById('lista');
    if (!list.length) {
        tb.innerHTML = '<tr><td colspan="7"><div class="empty-state"><p>Nenhuma matrícula</p></div></td></tr>';
        return;
    }

    tb.innerHTML = list.map(m => {
        const meta = SIT_META[m.situacao] || { css: '', label: m.situacao };

        // Botões contextuais por situação
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
                <button class="btn-action" style="color:#e74c3c"
                    onclick="abrirConfirmacao(${m.id},'cancelar','Cancelar Matrícula')"
                    title="Cancelar"><i class="bi bi-x-circle"></i></button>
                <button class="btn-action" style="color:#27ae60"
                    onclick="confirmarConcluir(${m.id})"
                    title="Concluir"><i class="bi bi-check-circle"></i></button>`;
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
    const alunos = await (await fetch('/api/alunos?size=500&ativo=true', { headers: h() })).json();
    const turmas = await (await fetch('/api/turmas?size=100&ativo=true', { headers: h() })).json();
    document.getElementById('matAluno').innerHTML =
        '<option value="">Selecione...</option>' +
        (alunos.content || []).map(a => `<option value="${a.id}">${a.ra} - ${a.nome}</option>`).join('');
    document.getElementById('matTurma').innerHTML =
        '<option value="">Selecione...</option>' +
        (turmas.content || []).map(t => `<option value="${t.id}">${t.identificacao}</option>`).join('');
}

function abrirModal() {
    new bootstrap.Modal(document.getElementById('modalMatricula')).show();
}

async function salvar() {
    const body = {
        alunoId: parseInt(document.getElementById('matAluno').value),
        turmaId: parseInt(document.getElementById('matTurma').value)
    };
    const res = await fetch(API, { method: 'POST', headers: h(), body: JSON.stringify(body) });
    if (!res.ok) { alert(await extrairMensagem(res)); return; }
    bootstrap.Modal.getInstance(document.getElementById('modalMatricula')).hide();
    buscar();
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
        const res = await fetch(url, {
            method: 'PATCH',
            headers: h(),
            body: JSON.stringify({ motivo })
        });

        if (res.status === 401) {
            alert('Sessão expirada. Faça login novamente.');
            window.location.href = '/login';
            return;
        }

        if (!res.ok) {
            const msg = await extrairMensagem(res);
            alert('Erro: ' + msg);
            return;
        }

        // Fecha o modal com segurança
        const modalEl = document.getElementById('modalConfirmacao');
        const bsModal = bootstrap.Modal.getInstance(modalEl);
        if (bsModal) bsModal.hide();

        buscar();

    } catch (e) {
        console.error('[executarAcao] Erro:', e);
        alert('Erro de rede ao executar a ação. Verifique o console (F12) para detalhes.');
    } finally {
        btnConfirmar.disabled = false;
        btnConfirmar.innerHTML = '<i class="bi bi-check-lg me-1"></i>Confirmar';
    }
}

async function confirmarConcluir(id) {
    if (!confirm('Concluir esta matrícula? Esta ação indica que o aluno finalizou o ano letivo.')) return;

    try {
        const res = await fetch(`${API}/${id}/concluir`, {
            method: 'PATCH',
            headers: h(),
            // Envia body vazio mas válido para evitar rejeição do servidor
            body: JSON.stringify({})
        });

        if (res.status === 401) {
            alert('Sessão expirada. Faça login novamente.');
            window.location.href = '/login';
            return;
        }

        if (!res.ok) {
            const msg = await extrairMensagem(res);
            alert('Erro: ' + msg);
            return;
        }

        buscar();
    } catch (e) {
        console.error('[confirmarConcluir] Erro:', e);
        alert('Erro de rede ao concluir. Verifique o console (F12) para detalhes.');
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

/**
 * Tenta extrair uma mensagem legível de qualquer resposta de erro da API,
 * sem lançar exceções adicionais caso o corpo não seja JSON.
 */
async function extrairMensagem(res) {
    try {
        const data = await res.json();
        // O GlobalExceptionHandler retorna chave "erro"; outros endpoints podem usar "message"
        return data.erro || data.message || data.error || JSON.stringify(data);
    } catch (_) {
        // Corpo não é JSON (ex: Whitelabel Error Page do Spring)
        return `HTTP ${res.status} ${res.statusText}`;
    }
}
