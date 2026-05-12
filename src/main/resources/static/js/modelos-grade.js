// JS de Modelos de Grade
const API = '/api/modelos-grade';
const DIAS = ['MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY'];
const DIAS_PT = ['Segunda','Terça','Quarta','Quinta','Sexta'];
let disciplinas = [];
let modeloAtual = null;

document.addEventListener('DOMContentLoaded', async () => {
    if (!localStorage.getItem('token')) { window.location.href = '/login'; return; }
    disciplinas = await carregarDisciplinas();
    gerarLinhasGrade();
    buscar();
});

async function carregarDisciplinas() {
    const res = await apiFetch('/api/disciplinas?size=100&ativo=true');
    if (!res.ok) return [];
    const data = await res.json();
    return data.content || [];
}

async function buscar() {
    const ano = document.getElementById('filtroAno').value;
    const container = document.getElementById('modelosContainer');
    container.innerHTML = '<div class="text-center py-4"><div class="spinner-border text-primary"></div></div>';
    const res = await apiFetch(`${API}?anoLetivo=${ano}`);
    if (!res.ok) { container.innerHTML = '<div class="alert alert-danger">Erro ao carregar modelos.</div>'; return; }
    const lista = await res.json();
    renderizar(lista);
}

function renderizar(lista) {
    const container = document.getElementById('modelosContainer');
    if (!lista.length) {
        container.innerHTML = `<div class="empty-state"><i class="bi bi-grid-3x3-gap d-block"></i><p>Nenhum modelo cadastrado para este ano</p></div>`;
        return;
    }

    // Agrupa por série
    const porSerie = {};
    lista.forEach(m => {
        if (!porSerie[m.serie]) porSerie[m.serie] = [];
        porSerie[m.serie].push(m);
    });

    let html = '';
    for (const serie of Object.keys(porSerie).sort()) {
        html += `<h6 class="fw-bold text-muted mt-3 mb-2"><i class="bi bi-mortarboard me-1"></i>${serie}º Ano</h6>
        <div class="row g-3 mb-3">`;
        porSerie[serie].forEach(m => {
            html += `
            <div class="col-md-6 col-lg-4 animate-in">
                <div class="data-card p-3 h-100" style="border-left: 4px solid var(--primary)">
                    <div class="d-flex justify-content-between align-items-start mb-2">
                        <div>
                            <span class="fw-bold">${m.nome}</span>
                            <span class="badge bg-primary ms-2">${m.serie}º Ano</span>
                        </div>
                        <div class="d-flex gap-1">
                            <button class="btn btn-sm btn-outline-primary" onclick="visualizarModelo(${m.id})" title="Visualizar grade">
                                <i class="bi bi-eye"></i>
                            </button>
                            <button class="btn btn-sm btn-outline-secondary" onclick="editarModelo(${m.id})" title="Editar">
                                <i class="bi bi-pencil"></i>
                            </button>
                            <button class="btn btn-sm btn-outline-danger" onclick="deletarModelo(${m.id}, '${m.nome}')" title="Excluir">
                                <i class="bi bi-trash"></i>
                            </button>
                        </div>
                    </div>
                    <small class="text-muted">${m.aulas ? m.aulas.length : 0} aulas configuradas</small>
                </div>
            </div>`;
        });
        html += '</div>';
    }
    container.innerHTML = html;
}

function gerarLinhasGrade() {
    const tbody = document.getElementById('gradeBody');
    const optionsHtml = '<option value="">—</option>' +
        disciplinas.map(d => `<option value="${d.id}">${d.descricao}</option>`).join('');

    let html = '';
    for (let aula = 1; aula <= 5; aula++) {
        html += `<tr><td class="fw-bold">Aula ${aula}</td>`;
        DIAS.forEach(dia => {
            html += `<td><select class="form-select form-select-sm" id="grade_${dia}_${aula}" style="min-width:120px">${optionsHtml}</select></td>`;
        });
        html += '</tr>';
    }
    tbody.innerHTML = html;
}

function abrirModalNovo() {
    modeloAtual = null;
    document.getElementById('modalTitle').textContent = 'Novo Modelo de Grade';
    document.getElementById('modeloId').value = '';
    document.getElementById('modeloAno').value = document.getElementById('filtroAno').value;
    document.getElementById('modeloSerie').value = '1';
    document.getElementById('modeloNome').value = '';
    // Limpa a grade
    for (let aula = 1; aula <= 5; aula++) {
        DIAS.forEach(dia => {
            const sel = document.getElementById(`grade_${dia}_${aula}`);
            if (sel) sel.value = '';
        });
    }
    new bootstrap.Modal(document.getElementById('modalModelo')).show();
}

async function editarModelo(id) {
    const res = await apiFetch(`${API}/${id}`);
    if (!res.ok) { alert('Erro ao carregar modelo.'); return; }
    modeloAtual = await res.json();

    document.getElementById('modalTitle').textContent = 'Editar Modelo de Grade';
    document.getElementById('modeloId').value = modeloAtual.id;
    document.getElementById('modeloAno').value = modeloAtual.anoLetivo;
    document.getElementById('modeloSerie').value = modeloAtual.serie;
    document.getElementById('modeloNome').value = modeloAtual.nome;

    // Preenche a grade
    for (let aula = 1; aula <= 5; aula++) {
        DIAS.forEach(dia => {
            const sel = document.getElementById(`grade_${dia}_${aula}`);
            if (sel) sel.value = '';
        });
    }
    if (modeloAtual.aulas) {
        modeloAtual.aulas.forEach(a => {
            const sel = document.getElementById(`grade_${a.diaSemana}_${a.numeroAula}`);
            if (sel && a.disciplina) sel.value = a.disciplina.id;
        });
    }

    new bootstrap.Modal(document.getElementById('modalModelo')).show();
}

async function visualizarModelo(id) {
    const res = await apiFetch(`${API}/${id}`);
    if (!res.ok) { alert('Erro ao carregar modelo.'); return; }
    const m = await res.json();

    document.getElementById('visualizarTitle').textContent = `${m.serie}º Ano — ${m.nome}`;

    // Monta grade visual
    const gradeMap = {};
    (m.aulas || []).forEach(a => {
        gradeMap[`${a.diaSemana}_${a.numeroAula}`] = a.disciplina?.descricao || '—';
    });

    let html = `<div class="table-responsive"><table class="table table-bordered text-center align-middle" style="font-size:13px">
        <thead class="table-dark"><tr><th>Aula</th>`;
    DIAS_PT.forEach(d => { html += `<th>${d}</th>`; });
    html += '</tr></thead><tbody>';
    for (let aula = 1; aula <= 5; aula++) {
        html += `<tr><td class="fw-bold">Aula ${aula}</td>`;
        DIAS.forEach(dia => {
            const nome = gradeMap[`${dia}_${aula}`] || '—';
            const cor = corDisciplina(nome);
            html += `<td style="background:${cor};font-weight:500">${nome}</td>`;
        });
        html += '</tr>';
    }
    html += '</tbody></table></div>';
    document.getElementById('visualizarBody').innerHTML = html;
    new bootstrap.Modal(document.getElementById('modalVisualizar')).show();
}

function corDisciplina(nome) {
    const mapa = {
        'Português': '#dbeafe', 'Matemática': '#fef3c7', 'Ciências': '#d1fae5',
        'História': '#fce7f3', 'Geografia': '#e0e7ff', 'Educação Física': '#fee2e2',
        'Artes': '#fde68a', 'Informática': '#cffafe'
    };
    for (const [key, cor] of Object.entries(mapa)) {
        if (nome.includes(key.split(' ')[0])) return cor;
    }
    return '#f9fafb';
}

async function salvarModelo() {
    const id = document.getElementById('modeloId').value;
    const aulas = [];
    for (let aula = 1; aula <= 5; aula++) {
        DIAS.forEach(dia => {
            const sel = document.getElementById(`grade_${dia}_${aula}`);
            if (sel && sel.value) {
                aulas.push({ diaSemana: dia, numeroAula: aula, disciplinaId: parseInt(sel.value) });
            }
        });
    }

    const body = {
        anoLetivo: parseInt(document.getElementById('modeloAno').value),
        serie: parseInt(document.getElementById('modeloSerie').value),
        nome: document.getElementById('modeloNome').value.trim(),
        aulas
    };

    if (!body.nome) { alert('Informe o nome do modelo.'); return; }

    const res = await apiFetch(id ? `${API}/${id}` : API, {
        method: id ? 'PUT' : 'POST',
        body: JSON.stringify(body)
    });

    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        alert(err.message || 'Erro ao salvar modelo.');
        return;
    }

    bootstrap.Modal.getInstance(document.getElementById('modalModelo')).hide();
    buscar();
}

async function deletarModelo(id, nome) {
    if (!confirm(`Excluir o modelo "${nome}"? Turmas que usam este modelo perderão a referência.`)) return;
    const res = await apiFetch(`${API}/${id}`, { method: 'DELETE' });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        alert(err.message || 'Erro ao excluir modelo.');
        return;
    }
    buscar();
}
