//JS da usuário
const API = '/api/usuarios';
const h = () => ({ 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + localStorage.getItem('token') });

document.addEventListener('DOMContentLoaded', () => { if (!localStorage.getItem('token')) { window.location.href='/login'; return; } buscar(); });

async function buscar() {
    const nome = document.getElementById('campoBusca').value;
    let url = `${API}?size=50&sort=nomeCompleto`;
    if (nome) url += `&nome=${encodeURIComponent(nome)}`;
    const data = await (await fetch(url, { headers: h() })).json();
    renderizar(data.content || []);
}

const roleLabels = { ADMIN: 'Administrador', COORDENADOR: 'Coordenador', OPERADOR: 'Operador', PROFESSOR: 'Professor' };

function renderizar(list) {
    const tb = document.getElementById('lista');
    if (!list.length) { tb.innerHTML = '<tr><td colspan="6"><div class="empty-state"><p>Nenhum usuário</p></div></td></tr>'; return; }
    tb.innerHTML = list.map(u => `<tr>
        <td><strong>${u.codigo}</strong></td><td>${u.nomeCompleto}</td><td>${u.login}</td>
        <td>${roleLabels[u.role]||u.role}</td>
        <td><span class="badge-status ${u.ativo?(u.bloqueado?'trancada':'ativo'):'inativo'}">${u.bloqueado?'Bloqueado':(u.ativo?'Ativo':'Inativo')}</span></td>
        <td>
            <button class="btn-action edit" onclick="editar(${u.id})"><i class="bi bi-pencil"></i></button>
            ${u.bloqueado?`<button class="btn-action view" onclick="desbloquear(${u.id})" title="Desbloquear"><i class="bi bi-unlock"></i></button>`:''}
            <button class="btn-action delete" onclick="inativar(${u.id})" title="Inativar"><i class="bi bi-pause-circle"></i></button>
        </td></tr>`).join('');
}

function abrirModal(u) {
    document.getElementById('modalTitle').textContent = u ? 'Editar Usuário' : 'Novo Usuário';
    document.getElementById('userId').value = u?.id || '';
    document.getElementById('userNome').value = u?.nomeCompleto || '';
    document.getElementById('userEmail').value = u?.email || '';
    document.getElementById('userLogin').value = u?.login || '';
    document.getElementById('userSenha').value = '';
    document.getElementById('userRole').value = u?.role || 'OPERADOR';
    new bootstrap.Modal(document.getElementById('modalUsuario')).show();
}

async function editar(id) { abrirModal(await (await fetch(`${API}/${id}`, { headers: h() })).json()); }

async function salvar() {
    const id = document.getElementById('userId').value;
    const body = { nomeCompleto: document.getElementById('userNome').value, email: document.getElementById('userEmail').value,
        login: document.getElementById('userLogin').value, senha: document.getElementById('userSenha').value || 'siga2026',
        role: document.getElementById('userRole').value };
    const res = await fetch(id ? `${API}/${id}` : API, { method: id?'PUT':'POST', headers: h(), body: JSON.stringify(body) });
    if (!res.ok) { alert((await res.json()).message || 'Erro'); return; }
    bootstrap.Modal.getInstance(document.getElementById('modalUsuario')).hide();
    buscar();
}

async function inativar(id) { if (!confirm('Inativar?')) return; await fetch(`${API}/${id}/inativar`, { method: 'PATCH', headers: h() }); buscar(); }
async function desbloquear(id) { await fetch(`${API}/${id}/desbloquear`, { method: 'PATCH', headers: h() }); buscar(); }
