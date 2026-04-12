//JS da matricula
const API = '/api/matriculas';
const h = () => ({ 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + localStorage.getItem('token') });

document.addEventListener('DOMContentLoaded', () => { if (!localStorage.getItem('token')) { window.location.href='/login'; return; } buscar(); carregarSelects(); });

async function buscar() {
    const data = await (await fetch(`${API}?size=50`, { headers: h() })).json();
    renderizar(data.content || []);
}

function renderizar(list) {
    const tb = document.getElementById('lista');
    if (!list.length) { tb.innerHTML = '<tr><td colspan="7"><div class="empty-state"><p>Nenhuma matrícula</p></div></td></tr>'; return; }
    const sit = { ATIVA: 'ativo', TRANCADA: 'trancada', CANCELADA: 'cancelada' };
    tb.innerHTML = list.map(m => `<tr>
        <td><strong>${m.numero}</strong></td><td>${m.alunoNome}</td><td>${m.alunoRa}</td><td>${m.turmaIdentificacao}</td>
        <td>${m.anoLetivo}</td><td><span class="badge-status ${sit[m.situacao]||''}">${m.situacao}</span></td>
        <td>${m.situacao==='ATIVA'?`<button class="btn-action delete" onclick="trancar(${m.id})" title="Trancar"><i class="bi bi-pause-circle"></i></button>`:''}</td></tr>`).join('');
}

async function carregarSelects() {
    const alunos = await (await fetch('/api/alunos?size=500&ativo=true', { headers: h() })).json();
    const turmas = await (await fetch('/api/turmas?size=100&ativo=true', { headers: h() })).json();
    document.getElementById('matAluno').innerHTML = '<option value="">Selecione...</option>' + (alunos.content||[]).map(a => `<option value="${a.id}">${a.ra} - ${a.nome}</option>`).join('');
    document.getElementById('matTurma').innerHTML = '<option value="">Selecione...</option>' + (turmas.content||[]).map(t => `<option value="${t.id}">${t.identificacao}</option>`).join('');
}

function abrirModal() { new bootstrap.Modal(document.getElementById('modalMatricula')).show(); }

async function salvar() {
    const body = { alunoId: parseInt(document.getElementById('matAluno').value), turmaId: parseInt(document.getElementById('matTurma').value) };
    const res = await fetch(API, { method: 'POST', headers: h(), body: JSON.stringify(body) });
    if (!res.ok) { alert((await res.json()).message || 'Erro'); return; }
    bootstrap.Modal.getInstance(document.getElementById('modalMatricula')).hide();
    buscar();
}

async function trancar(id) { if (!confirm('Trancar matrícula?')) return; await fetch(`${API}/${id}/trancar`, { method: 'PATCH', headers: h(), body: JSON.stringify({ motivo: 'Trancamento solicitado' }) }); buscar(); }
