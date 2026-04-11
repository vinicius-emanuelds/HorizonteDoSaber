const API = '/api/disciplinas';
const h = () => ({ 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + localStorage.getItem('token') });

document.addEventListener('DOMContentLoaded', () => { if (!localStorage.getItem('token')) { window.location.href='/login'; return; } buscar(); });

async function buscar() {
    const desc = document.getElementById('campoBusca').value;
    let url = `${API}?size=50&sort=descricao`;
    if (desc) url += `&descricao=${encodeURIComponent(desc)}`;
    const data = await (await fetch(url, { headers: h() })).json();
    renderizar(data.content || []);
}

function renderizar(list) {
    const tb = document.getElementById('lista');
    if (!list.length) { tb.innerHTML = '<tr><td colspan="5"><div class="empty-state"><p>Nenhuma disciplina</p></div></td></tr>'; return; }
    tb.innerHTML = list.map(d => `<tr>
        <td><strong>${d.codigo}</strong></td><td>${d.descricao}</td><td>${d.cargaHorariaAnual}h</td>
        <td><span class="badge-status ${d.ativo?'ativo':'inativo'}">${d.ativo?'Ativa':'Inativa'}</span></td>
        <td><button class="btn-action edit" onclick="editar(${d.id})"><i class="bi bi-pencil"></i></button>
            <button class="btn-action delete" onclick="inativar(${d.id})"><i class="bi bi-pause-circle"></i></button></td></tr>`).join('');
}

function abrirModal(d) {
    document.getElementById('modalTitle').textContent = d ? 'Editar Disciplina' : 'Nova Disciplina';
    document.getElementById('discId').value = d?.id || '';
    document.getElementById('discDescricao').value = d?.descricao || '';
    document.getElementById('discCarga').value = d?.cargaHorariaAnual || '';
    new bootstrap.Modal(document.getElementById('modalDisciplina')).show();
}

async function editar(id) { abrirModal(await (await fetch(`${API}/${id}`, { headers: h() })).json()); }

async function salvar() {
    const id = document.getElementById('discId').value;
    const body = { descricao: document.getElementById('discDescricao').value, cargaHorariaAnual: parseInt(document.getElementById('discCarga').value) };
    const res = await fetch(id ? `${API}/${id}` : API, { method: id?'PUT':'POST', headers: h(), body: JSON.stringify(body) });
    if (!res.ok) { alert((await res.json()).message || 'Erro'); return; }
    bootstrap.Modal.getInstance(document.getElementById('modalDisciplina')).hide();
    buscar();
}

async function inativar(id) { if(!confirm('Inativar?')) return; await fetch(`${API}/${id}/inativar`,{method:'PATCH',headers:h()}); buscar(); }
