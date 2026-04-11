const API = '/api/alunos';
const headers = () => ({ 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + localStorage.getItem('token') });

document.addEventListener('DOMContentLoaded', () => {
    if (!localStorage.getItem('token')) { window.location.href = '/login'; return; }
    buscarAlunos();
});

async function buscarAlunos() {
    const nome = document.getElementById('campoBusca').value;
    const ativo = document.getElementById('filtroStatus').value;
    let url = `${API}?size=50&sort=nome`;
    if (nome) url += `&nome=${encodeURIComponent(nome)}`;
    if (ativo) url += `&ativo=${ativo}`;
    try {
        const res = await fetch(url, { headers: headers() });
        const data = await res.json();
        renderizar(data.content || []);
    } catch (e) { console.error(e); }
}

function renderizar(alunos) {
    const tbody = document.getElementById('listaAlunos');
    if (alunos.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7"><div class="empty-state"><i class="bi bi-people d-block"></i><p>Nenhum aluno encontrado</p></div></td></tr>';
        return;
    }
    tbody.innerHTML = alunos.map(a => `<tr>
        <td><strong>${a.ra}</strong></td>
        <td>${a.nome}</td>
        <td>${formatCpf(a.cpf)}</td>
        <td>${a.email}</td>
        <td>${a.nomeResponsavel}</td>
        <td><span class="badge-status ${a.ativo ? 'ativo' : 'inativo'}">${a.ativo ? 'Ativo' : 'Inativo'}</span></td>
        <td>
            <button class="btn-action edit" onclick="editar(${a.id})" title="Editar"><i class="bi bi-pencil"></i></button>
            ${a.ativo ?
                `<button class="btn-action delete" onclick="inativar(${a.id})" title="Inativar"><i class="bi bi-pause-circle"></i></button>` :
                `<button class="btn-action view" onclick="ativar(${a.id})" title="Ativar"><i class="bi bi-play-circle"></i></button>`}
        </td>
    </tr>`).join('');
}

function formatCpf(v) { return v ? v.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4') : ''; }

function abrirModal(a) {
    document.getElementById('modalAlunoTitle').textContent = a ? 'Editar Aluno' : 'Novo Aluno';
    document.getElementById('alunoId').value = a ? a.id : '';
    document.getElementById('alunoNome').value = a ? a.nome : '';
    document.getElementById('alunoDataNasc').value = a ? a.dataNascimento : '';
    document.getElementById('alunoCpf').value = a ? a.cpf : '';
    document.getElementById('alunoEmail').value = a ? a.email : '';
    document.getElementById('alunoResponsavel').value = a ? a.nomeResponsavel : '';
    document.getElementById('alunoCpfResp').value = a ? a.cpfResponsavel : '';
    new bootstrap.Modal(document.getElementById('modalAluno')).show();
}

async function editar(id) {
    const res = await fetch(`${API}/${id}`, { headers: headers() });
    const a = await res.json();
    abrirModal(a);
}

async function salvarAluno() {
    const id = document.getElementById('alunoId').value;
    const body = {
        nome: document.getElementById('alunoNome').value,
        dataNascimento: document.getElementById('alunoDataNasc').value,
        cpf: document.getElementById('alunoCpf').value,
        email: document.getElementById('alunoEmail').value,
        nomeResponsavel: document.getElementById('alunoResponsavel').value,
        cpfResponsavel: document.getElementById('alunoCpfResp').value
    };
    const url = id ? `${API}/${id}` : API;
    const method = id ? 'PUT' : 'POST';
    try {
        const res = await fetch(url, { method, headers: headers(), body: JSON.stringify(body) });
        if (!res.ok) { const e = await res.json(); alert(e.message || 'Erro'); return; }
        bootstrap.Modal.getInstance(document.getElementById('modalAluno')).hide();
        buscarAlunos();
    } catch (e) { alert('Erro ao salvar'); }
}

async function inativar(id) {
    if (!confirm('Deseja inativar este aluno?')) return;
    await fetch(`${API}/${id}/inativar`, { method: 'PATCH', headers: headers() });
    buscarAlunos();
}

async function ativar(id) {
    await fetch(`${API}/${id}/ativar`, { method: 'PATCH', headers: headers() });
    buscarAlunos();
}
