//JS da turma
const API = '/api/turmas';

document.addEventListener('DOMContentLoaded', () => { 
    if (!localStorage.getItem('token')) { window.location.href='/login'; return; } 
    buscar(); 
    carregarProfessores(); 
    carregarModelos();
    carregarDisciplinasEspecificas();
});

let disciplinasEspecificas = {};

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
    const options = '<option value="">Selecione...</option>' + (data.content || []).map(p => `<option value="${p.id}">${p.nome}</option>`).join('');
    document.getElementById('turmaProf').innerHTML = options;
    document.getElementById('profArtes').innerHTML = options;
    document.getElementById('profEdFisica').innerHTML = options;
    document.getElementById('profInformatica').innerHTML = options;
}

async function carregarModelos() {
    const ano = document.getElementById('turmaAno').value || 2026;
    const res = await apiFetch(`/api/modelos-grade?anoLetivo=${ano}`);
    if (res.ok) {
        const data = await res.json();
        document.getElementById('turmaModelo').innerHTML = '<option value="">Selecione...</option>' + 
            data.map(m => `<option value="${m.id}">${m.serie}º Ano - ${m.nome}</option>`).join('');
    }
}

async function carregarDisciplinasEspecificas() {
    const res = await apiFetch('/api/disciplinas?size=100');
    if (res.ok) {
        const data = await res.json();
        const lista = data.content || [];
        const dArtes = lista.find(d => d.descricao.toLowerCase().includes('artes'));
        const dEdFisica = lista.find(d => d.descricao.toLowerCase().includes('física') || d.descricao.toLowerCase().includes('fisica'));
        const dInfo = lista.find(d => d.descricao.toLowerCase().includes('informática') || d.descricao.toLowerCase().includes('informatica'));
        
        if (dArtes) disciplinasEspecificas.artes = dArtes.id;
        if (dEdFisica) disciplinasEspecificas.edFisica = dEdFisica.id;
        if (dInfo) disciplinasEspecificas.informatica = dInfo.id;
    }
}

document.getElementById('turmaAno').addEventListener('change', carregarModelos);
document.getElementById('turmaSerie').addEventListener('change', carregarModelos);

function abrirModal(t) {
    document.getElementById('modalTitle').textContent = t ? 'Editar Turma' : 'Nova Turma';
    document.getElementById('turmaId').value = t?.id || '';
    document.getElementById('turmaAno').value = t?.anoLetivo || 2026;
    document.getElementById('turmaSerie').value = t?.serie || 1;
    document.getElementById('turmaNome').value = t?.nome || '';
    document.getElementById('turmaTurno').value = t?.turno || 'MATUTINO';
    if (t?.professorRegenteId) document.getElementById('turmaProf').value = t.professorRegenteId;
    if (t?.modeloGradeId) document.getElementById('turmaModelo').value = t.modeloGradeId;
    
    // Na edição, carregar específicos. Como não vem do response simples, deixar limpo por enquanto, 
    // ou idealmente buscar detalhes. Para o MVP de hoje, deixamos vazio exigindo resseleção.
    document.getElementById('profArtes').value = '';
    document.getElementById('profEdFisica').value = '';
    document.getElementById('profInformatica').value = '';
    
    carregarModelos();
    new bootstrap.Modal(document.getElementById('modalTurma')).show();
}

async function editarCard(id) { abrirModal(await (await apiFetch(`${API}/${id}`)).json()); }

async function salvar() {
    const id = document.getElementById('turmaId').value;
    
    const esp = [];
    if (disciplinasEspecificas.artes && document.getElementById('profArtes').value) {
        esp.push({ disciplinaId: disciplinasEspecificas.artes, professorId: parseInt(document.getElementById('profArtes').value) });
    }
    if (disciplinasEspecificas.edFisica && document.getElementById('profEdFisica').value) {
        esp.push({ disciplinaId: disciplinasEspecificas.edFisica, professorId: parseInt(document.getElementById('profEdFisica').value) });
    }
    if (disciplinasEspecificas.informatica && document.getElementById('profInformatica').value) {
        esp.push({ disciplinaId: disciplinasEspecificas.informatica, professorId: parseInt(document.getElementById('profInformatica').value) });
    }
    
    const body = {
        anoLetivo: parseInt(document.getElementById('turmaAno').value),
        serie: parseInt(document.getElementById('turmaSerie').value),
        nome: document.getElementById('turmaNome').value,
        turno: document.getElementById('turmaTurno').value,
        professorRegenteId: parseInt(document.getElementById('turmaProf').value),
        modeloGradeId: parseInt(document.getElementById('turmaModelo').value),
        professoresEspecificos: esp
    };
    
    const res = await apiFetch(id ? `${API}/${id}` : API, { method: id?'PUT':'POST', body: JSON.stringify(body) });
    if (!res.ok) { alert((await res.json()).message || 'Erro'); return; }
    bootstrap.Modal.getInstance(document.getElementById('modalTurma')).hide();
    buscar();
}
