/**
 * ano-letivo.js
 * Gerenciamento da página de Administração do Ano Letivo.
 *
 * Estrutura:
 * - 5 aulas de 45 min por turno, intervalo após a 3ª aula (~4h de aula)
 * - Frequência registrada por aula (não por dia)
 * - Bloqueio automático de lançamentos ao encerrar o ano
 */

const API = '/api/anos-letivos';

// ─────────────────────── Estado local ───────────────────────
let feriadosList = [];        // LocalDate strings do modal
let semanasAvaliacaoList = []; // Objetos {bimestre, tipo, dataInicio, dataFim}
let anoIdParaEncerrar = null;

// ─────────────────────── Init ───────────────────────
document.addEventListener('DOMContentLoaded', () => {
    carregarAnos();
});

// ─────────────────────── API calls ───────────────────────

function getToken() {
    return localStorage.getItem('token') || '';
}

async function fetchAPI(url, options = {}) {
    const res = await fetch(url, {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + getToken(),
            ...(options.headers || {})
        },
        ...options
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({ message: res.statusText }));
        throw new Error(err.message || 'Erro na requisição');
    }
    if (res.status === 204) return null;
    return res.json();
}

// ─────────────────────── Carregar lista ───────────────────────

async function carregarAnos() {
    try {
        const anos = await fetchAPI(API);
        renderizarTabela(anos);
    } catch (e) {
        mostrarAlerta('danger', 'Erro ao carregar anos letivos: ' + e.message);
    }
}

function renderizarTabela(anos) {
    const tbody = document.getElementById('listaAnos');
    if (!anos || anos.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" class="text-center text-muted py-4">
            <i class="bi bi-inbox fs-3 d-block mb-2"></i>Nenhum ano letivo cadastrado.</td></tr>`;
        return;
    }

    const isAdmin = document.getElementById('btnNovoAno') !== null;

    tbody.innerHTML = anos.map(a => {
        const statusBadge = a.encerrado
            ? `<span class="badge bg-danger"><i class="bi bi-lock-fill me-1"></i>Encerrado</span>`
            : `<span class="badge bg-success"><i class="bi bi-unlock-fill me-1"></i>Ativo</span>`;

        const acoes = isAdmin ? `
            <div class="d-flex gap-1">
                <button class="btn btn-sm btn-outline-primary" title="Ver detalhes"
                        onclick="verDetalhes(${a.id})">
                    <i class="bi bi-eye"></i>
                </button>
                ${!a.encerrado ? `
                <button class="btn btn-sm btn-outline-secondary" title="Editar"
                        onclick="abrirModalEditar(${a.id})">
                    <i class="bi bi-pencil"></i>
                </button>
                <button class="btn btn-sm btn-outline-danger" title="Encerrar ano letivo"
                        onclick="pedirConfirmacaoEncerramento(${a.id}, ${a.ano})">
                    <i class="bi bi-lock-fill"></i>
                </button>
                ` : `
                <button class="btn btn-sm btn-outline-primary" title="Ver detalhes"
                        onclick="verDetalhes(${a.id})" disabled>
                    <i class="bi bi-pencil"></i>
                </button>
                `}
            </div>` : `
            <button class="btn btn-sm btn-outline-primary" title="Ver detalhes"
                    onclick="verDetalhes(${a.id})">
                <i class="bi bi-eye"></i>
            </button>`;

        return `<tr>
            <td><strong>${a.ano}</strong></td>
            <td>${formatarData(a.dataInicio)}</td>
            <td>${formatarData(a.dataEncerramento)}</td>
            <td>${a.diasLetivos ?? '—'}</td>
            <td>
                <span class="badge bg-secondary">${(a.feriados || []).length} data(s)</span>
            </td>
            <td>
                <span class="badge bg-info text-dark">${(a.semanasAvaliacao || []).length} semana(s)</span>
            </td>
            <td>${statusBadge}</td>
            <td>${acoes}</td>
        </tr>`;
    }).join('');
}

// ─────────────────────── Modal Novo ───────────────────────

function abrirModalNovo() {
    document.getElementById('anoId').value = '';
    document.getElementById('anoNumero').value = '';
    document.getElementById('anoDataInicio').value = '';
    document.getElementById('anoDataEncerramento').value = '';
    document.getElementById('anoDiasLetivos').value = '';
    document.getElementById('modalAnoLetivoTitle').textContent = 'Novo Ano Letivo';
    document.getElementById('btnSalvarAno').innerHTML = '<i class="bi bi-floppy me-1"></i> Salvar';

    feriadosList = [];
    semanasAvaliacaoList = [];
    renderizarFeriados();
    renderizarSemanasAvaliacao();

    new bootstrap.Modal(document.getElementById('modalAnoLetivo')).show();
}

// ─────────────────────── Modal Editar ───────────────────────

async function abrirModalEditar(id) {
    try {
        const ano = await fetchAPI(`${API}/${id}`);
        document.getElementById('anoId').value = ano.id;
        document.getElementById('anoNumero').value = ano.ano;
        document.getElementById('anoDataInicio').value = ano.dataInicio;
        document.getElementById('anoDataEncerramento').value = ano.dataEncerramento;
        document.getElementById('anoDiasLetivos').value = ano.diasLetivos ?? '';
        document.getElementById('modalAnoLetivoTitle').textContent = `Editar Ano Letivo ${ano.ano}`;
        document.getElementById('btnSalvarAno').innerHTML = '<i class="bi bi-floppy me-1"></i> Atualizar';

        feriadosList = [...(ano.feriados || [])];
        semanasAvaliacaoList = [...(ano.semanasAvaliacao || [])];
        renderizarFeriados();
        renderizarSemanasAvaliacao();

        new bootstrap.Modal(document.getElementById('modalAnoLetivo')).show();
    } catch (e) {
        mostrarAlerta('danger', 'Erro ao carregar dados: ' + e.message);
    }
}

// ─────────────────────── Salvar ───────────────────────

async function salvarAnoLetivo() {
    const id = document.getElementById('anoId').value;
    const payload = {
        ano: parseInt(document.getElementById('anoNumero').value),
        dataInicio: document.getElementById('anoDataInicio').value,
        dataEncerramento: document.getElementById('anoDataEncerramento').value,
        diasLetivos: document.getElementById('anoDiasLetivos').value
            ? parseInt(document.getElementById('anoDiasLetivos').value) : null,
        feriados: feriadosList,
        semanasAvaliacao: semanasAvaliacaoList
    };

    if (!payload.ano || !payload.dataInicio || !payload.dataEncerramento) {
        mostrarAlerta('warning', 'Preencha todos os campos obrigatórios (*).');
        return;
    }
    if (payload.dataEncerramento < payload.dataInicio) {
        mostrarAlerta('warning', 'A data de encerramento deve ser posterior à data de início.');
        return;
    }
    for (const s of payload.semanasAvaliacao) {
        if (!s.dataInicio || !s.dataFim) {
            mostrarAlerta('warning', 'Preencha as datas de início e fim para todas as semanas de avaliação.');
            return;
        }
        if (s.dataFim < s.dataInicio) {
            mostrarAlerta('warning', 'A data final da avaliação deve ser posterior ou igual à inicial.');
            return;
        }
    }

    try {
        if (id) {
            await fetchAPI(`${API}/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
            mostrarAlerta('success', 'Ano letivo atualizado com sucesso!');
        } else {
            await fetchAPI(API, { method: 'POST', body: JSON.stringify(payload) });
            mostrarAlerta('success', 'Ano letivo cadastrado com sucesso!');
        }
        bootstrap.Modal.getInstance(document.getElementById('modalAnoLetivo'))?.hide();
        carregarAnos();
    } catch (e) {
        mostrarAlerta('danger', 'Erro ao salvar: ' + e.message);
    }
}

// ─────────────────────── Encerramento ───────────────────────

function pedirConfirmacaoEncerramento(id, ano) {
    anoIdParaEncerrar = id;
    document.getElementById('anoParaEncerrar').textContent = `Ano Letivo ${ano}`;
    new bootstrap.Modal(document.getElementById('modalConfirmarEncerramento')).show();
}

async function confirmarEncerramento() {
    if (!anoIdParaEncerrar) return;
    try {
        await fetchAPI(`${API}/${anoIdParaEncerrar}/encerrar`, { method: 'PATCH' });
        bootstrap.Modal.getInstance(document.getElementById('modalConfirmarEncerramento'))?.hide();
        mostrarAlerta('success', 'Ano letivo encerrado com sucesso! Lançamentos bloqueados.');
        carregarAnos();
    } catch (e) {
        mostrarAlerta('danger', 'Erro ao encerrar: ' + e.message);
    } finally {
        anoIdParaEncerrar = null;
    }
}

// ─────────────────────── Ver Detalhes ───────────────────────

async function verDetalhes(id) {
    try {
        const a = await fetchAPI(`${API}/${id}`);
        const statusIcon = a.encerrado
            ? `<span class="badge bg-danger fs-6"><i class="bi bi-lock-fill me-1"></i>Encerrado</span>`
            : `<span class="badge bg-success fs-6"><i class="bi bi-unlock-fill me-1"></i>Ativo</span>`;

        const feriadosHtml = (a.feriados && a.feriados.length > 0)
            ? `<div class="row g-2">${a.feriados.map(d =>
                `<div class="col-auto"><span class="badge bg-danger-subtle text-danger border border-danger-subtle">
                    <i class="bi bi-calendar-x me-1"></i>${formatarData(d)}</span></div>`
            ).join('')}</div>`
            : `<p class="text-muted small mb-0">Nenhum feriado cadastrado.</p>`;

        // Agrupar semanas de avaliação por bimestre
        let datasHtml = '';
        if (a.semanasAvaliacao && a.semanasAvaliacao.length > 0) {
            // Agrupar
            const porBimestre = {};
            a.semanasAvaliacao.forEach(s => {
                if(!porBimestre[s.bimestre]) porBimestre[s.bimestre] = [];
                porBimestre[s.bimestre].push(s);
            });
            
            datasHtml = `<div class="row g-3">` + Object.keys(porBimestre).map(bim => `
                <div class="col-md-6">
                    <div class="border rounded-3 p-3">
                        <div class="fw-semibold mb-2 text-primary">
                            <i class="bi bi-journal-bookmark me-1"></i>${bim}º Bimestre
                        </div>
                        ${porBimestre[bim].map(s => `
                            <div class="d-flex align-items-center justify-content-between mb-1 border-bottom pb-1">
                                <span class="badge bg-primary-subtle text-primary border border-primary-subtle">
                                    ${s.tipo}
                                </span>
                                <span class="small">${formatarData(s.dataInicio)} até ${formatarData(s.dataFim)}</span>
                            </div>`).join('')}
                    </div>
                </div>`).join('') + `</div>`;
        } else {
            datasHtml = `<p class="text-muted small mb-0">Nenhuma semana de avaliação cadastrada.</p>`;
        }

        document.getElementById('detalheAnoTitle').textContent = `Ano Letivo ${a.ano}`;
        document.getElementById('detalheAnoBody').innerHTML = `
            <div class="row g-3 mb-4">
                <div class="col-6 col-md-3">
                    <div class="text-muted small">Status</div>
                    <div class="mt-1">${statusIcon}</div>
                </div>
                <div class="col-6 col-md-3">
                    <div class="text-muted small">Início</div>
                    <div class="fw-semibold">${formatarData(a.dataInicio)}</div>
                </div>
                <div class="col-6 col-md-3">
                    <div class="text-muted small">Encerramento</div>
                    <div class="fw-semibold">${formatarData(a.dataEncerramento)}</div>
                </div>
                <div class="col-6 col-md-3">
                    <div class="text-muted small">Dias Letivos</div>
                    <div class="fw-semibold">${a.diasLetivos ?? '—'}</div>
                </div>
            </div>
            <div class="mb-4">
                <div class="alert alert-secondary d-flex gap-3 mb-0">
                    <div><i class="bi bi-clock me-1"></i><strong>Grade horária:</strong>
                        5 aulas de 45 min · Intervalo após a 3ª aula · Turno de ~4h</div>
                    <div><i class="bi bi-percent me-1"></i><strong>Frequência mínima:</strong> 75%</div>
                </div>
            </div>
            <div class="mb-4">
                <h6><i class="bi bi-calendar-x me-2 text-danger"></i>Feriados (${(a.feriados || []).length})</h6>
                ${feriadosHtml}
            </div>
            <div>
                <h6><i class="bi bi-pencil-square me-2 text-primary"></i>Semanas de Avaliação (${(a.semanasAvaliacao || []).length})</h6>
                ${datasHtml}
            </div>`;

        new bootstrap.Modal(document.getElementById('modalDetalheAno')).show();
    } catch (e) {
        mostrarAlerta('danger', 'Erro ao carregar detalhes: ' + e.message);
    }
}

// ─────────────────────── Feriados (modal) ───────────────────────

function adicionarFeriado() {
    feriadosList.push('');
    renderizarFeriados();
    // Foca no último input adicionado
    const inputs = document.querySelectorAll('.input-feriado');
    if (inputs.length > 0) inputs[inputs.length - 1].focus();
}

function removerFeriado(idx) {
    feriadosList.splice(idx, 1);
    renderizarFeriados();
}

function renderizarFeriados() {
    const container = document.getElementById('listaFeriados');
    const semMsg = document.getElementById('semFeriados');
    if (feriadosList.length === 0) {
        container.innerHTML = '';
        semMsg.style.display = '';
        return;
    }
    semMsg.style.display = 'none';
    container.innerHTML = feriadosList.map((d, i) => `
        <div class="col-md-3 col-sm-4">
            <div class="input-group input-group-sm">
                <input type="date" class="form-control input-feriado"
                       value="${d}" onchange="feriadosList[${i}] = this.value"
                       title="Feriado ${i + 1}">
                <button class="btn btn-outline-danger" type="button"
                        onclick="removerFeriado(${i})" title="Remover">
                    <i class="bi bi-x"></i>
                </button>
            </div>
        </div>`).join('');
}

// ─────────────────────── Semanas de Avaliação (modal) ───────────────────────

function adicionarSemanaAvaliacao() {
    semanasAvaliacaoList.push({
        bimestre: 1,
        tipo: 'AV1',
        dataInicio: '',
        dataFim: ''
    });
    renderizarSemanasAvaliacao();
}

function removerSemanaAvaliacao(idx) {
    semanasAvaliacaoList.splice(idx, 1);
    renderizarSemanasAvaliacao();
}

function renderizarSemanasAvaliacao() {
    const container = document.getElementById('listaDatasAvaliacao');
    const semMsg = document.getElementById('semDatasAv');
    if (semanasAvaliacaoList.length === 0) {
        container.innerHTML = '';
        semMsg.style.display = '';
        return;
    }
    semMsg.style.display = 'none';

    container.innerHTML = semanasAvaliacaoList.map((s, i) => `
        <div class="col-12 border rounded p-2 mb-2 bg-light">
            <div class="row g-2 align-items-end">
                <div class="col-md-2">
                    <label class="form-label small mb-0">Bimestre</label>
                    <select class="form-select form-select-sm" onchange="semanasAvaliacaoList[${i}].bimestre = parseInt(this.value)">
                        <option value="1" ${s.bimestre===1?'selected':''}>1º Bimestre</option>
                        <option value="2" ${s.bimestre===2?'selected':''}>2º Bimestre</option>
                        <option value="3" ${s.bimestre===3?'selected':''}>3º Bimestre</option>
                        <option value="4" ${s.bimestre===4?'selected':''}>4º Bimestre</option>
                    </select>
                </div>
                <div class="col-md-2">
                    <label class="form-label small mb-0">Tipo</label>
                    <select class="form-select form-select-sm" onchange="semanasAvaliacaoList[${i}].tipo = this.value">
                        <option value="AV1" ${s.tipo==='AV1'?'selected':''}>AV1</option>
                        <option value="AV2" ${s.tipo==='AV2'?'selected':''}>AV2</option>
                        <option value="REC" ${s.tipo==='REC'?'selected':''}>REC</option>
                    </select>
                </div>
                <div class="col-md-3">
                    <label class="form-label small mb-0">Semana Início</label>
                    <input type="date" class="form-control form-control-sm" value="${s.dataInicio}" onchange="semanasAvaliacaoList[${i}].dataInicio = this.value">
                </div>
                <div class="col-md-3">
                    <label class="form-label small mb-0">Semana Fim</label>
                    <input type="date" class="form-control form-control-sm" value="${s.dataFim}" onchange="semanasAvaliacaoList[${i}].dataFim = this.value">
                </div>
                <div class="col-md-2 text-end">
                    <button class="btn btn-sm btn-outline-danger w-100" type="button" onclick="removerSemanaAvaliacao(${i})">
                        <i class="bi bi-trash"></i> Remover
                    </button>
                </div>
            </div>
        </div>
    `).join('');
}

// ─────────────────────── Helpers ───────────────────────

function formatarData(iso) {
    if (!iso) return '—';
    const [y, m, d] = iso.split('-');
    return `${d}/${m}/${y}`;
}

function mostrarAlerta(tipo, mensagem) {
    const el = document.getElementById('statusAlert');
    el.innerHTML = `
        <div class="alert alert-${tipo} alert-dismissible fade show" role="alert">
            <i class="bi bi-${tipo === 'success' ? 'check-circle' : tipo === 'danger' ? 'x-circle' : 'exclamation-triangle'} me-2"></i>
            ${mensagem}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>`;
    el.style.display = '';
    // Auto-fechar após 5s (somente alertas de sucesso)
    if (tipo === 'success') {
        setTimeout(() => {
            const alert = el.querySelector('.alert');
            if (alert) bootstrap.Alert.getOrCreateInstance(alert).close();
        }, 5000);
    }
}
