//JS da turma
const API = '/api/turmas';

document.addEventListener('DOMContentLoaded', () => { if (!localStorage.getItem('token')) { window.location.href='/login'; return; } buscar(); carregarProfessores(); });

async function buscar() {
    const ano = document.getElementById('filtroAno').value;
    const data = await (await apiFetch(`${API}/ano/${ano}`)).json();
    renderizar(data);
}

function renderizar(list) {
    const c = document.getElementById('cards_turmas');
    if (!list.length) { c.innerHTML = '<div class="col-12"><div class="empty-state"><i class="bi bi-columns-gap d-block"></i><p>Nenhuma turma encontrada</p></div></div>'; return; }
    c.innerHTML = list.map(t => `<div class="col-md-3 col-sm-6 animate-in">
        <div class="turma-card" style="cursor:pointer" onclick="window.location.href='/turma-detalhe?id='+${t.id}">
            <div class="d-flex justify-content-between align-items-start">
                <div class="turma-serie">${t.serie}º</div>
                <button class="btn btn-sm btn-light" style="padding: 2px 6px" onclick="event.stopPropagation(); editarCard(${t.id})"><i class="bi bi-pencil"></i></button>
            </div>
            <div class="turma-nome">Turma ${t.nome}</div>
            <div class="turma-info mt-2"><i class="bi bi-clock me-1"></i>${t.turno === 'MATUTINO' ? 'Matutino' : 'Vespertino'}</div>
            <div class="turma-info"><i class="bi bi-person me-1"></i>${t.professorRegenteNome || 'Sem regente'}</div>
        </div>
    </div>`).join('');
}

async function carregarProfessores() {
    const data = await (await apiFetch('/api/professores?size=100&ativo=true')).json();
    const sel = document.getElementById('turmaProf');
    sel.innerHTML = '<option value="">Selecione...</option>' + (data.content || []).map(p => `<option value="${p.id}">${p.nome}</option>`).join('');
}

function abrirModal(t) {
    document.getElementById('modalTitle').textContent = t ? 'Editar Turma' : 'Nova Turma';
    document.getElementById('turmaId').value = t?.id || '';
    document.getElementById('turmaAno').value = t?.anoLetivo || 2026;
    document.getElementById('turmaSerie').value = t?.serie || 1;
    document.getElementById('turmaNome').value = t?.nome || '';
    document.getElementById('turmaTurno').value = t?.turno || 'MATUTINO';
    if (t?.professorRegenteId) document.getElementById('turmaProf').value = t.professorRegenteId;
    new bootstrap.Modal(document.getElementById('modalTurma')).show();
}

async function editarCard(id) { abrirModal(await (await apiFetch(`${API}/${id}`)).json()); }

async function salvar() {
    const id = document.getElementById('turmaId').value;
    const body = {
        anoLetivo: parseInt(document.getElementById('turmaAno').value),
        serie: parseInt(document.getElementById('turmaSerie').value),
        nome: document.getElementById('turmaNome').value,
        turno: document.getElementById('turmaTurno').value,
        professorRegenteId: parseInt(document.getElementById('turmaProf').value)
    };
    const res = await apiFetch(id ? `${API}/${id}` : API, { method: id?'PUT':'POST', body: JSON.stringify(body) });
    if (!res.ok) { alert((await res.json()).message || 'Erro'); return; }
    bootstrap.Modal.getInstance(document.getElementById('modalTurma')).hide();
    buscar();
}
