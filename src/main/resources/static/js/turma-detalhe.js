//JS da entidade turma-detalhe
const h = () => ({ 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + localStorage.getItem('token') });

const urlParams = new URLSearchParams(window.location.search);
const turmaId = urlParams.get('id');

document.addEventListener('DOMContentLoaded', () => {
    if (!localStorage.getItem('token')) { window.location.href='/login'; return; }
    if (!turmaId) { window.location.href='/turma'; return; }
    carregarDetalhes();
    carregarAlunos();
});

async function carregarDetalhes() {
    try {
        const t = await (await fetch(`/api/turmas/${turmaId}`, { headers: h() })).json();
        document.getElementById('detalheNome').textContent = `Turma ${t.serie}º ${t.nome}`;
        document.getElementById('detalheTurno').textContent = t.turno;
        document.getElementById('detalheAno').textContent = t.anoLetivo;
        document.getElementById('detalheProf').textContent = t.professorRegenteNome || 'Sem professor regente';
    } catch (e) {
        console.error(e);
        document.getElementById('detalheNome').textContent = 'Erro ao carregar turma';
    }
}

async function carregarAlunos() {
    try {
        const body = document.getElementById('listaAlunos');
        const matriculas = await (await fetch(`/api/matriculas/turma/${turmaId}`, { headers: h() })).json();
        document.getElementById('totalAlunos').textContent = `${matriculas.length} alunos`;
        
        if (!matriculas.length) {
            body.innerHTML = '<tr><td colspan="3" class="text-center text-muted">Nenhum aluno matriculado nesta turma.</td></tr>';
            return;
        }

        body.innerHTML = matriculas.map(m => `
            <tr>
                <td>${m.alunoRa}</td>
                <td class="fw-bold">${m.alunoNome}</td>
                <td><span class="badge ${m.situacao === 'ATIVA' ? 'bg-success' : 'bg-secondary'}">${m.situacao}</span></td>
            </tr>
        `).join('');
    } catch (e) {
        document.getElementById('listaAlunos').innerHTML = '<tr><td colspan="3" class="text-center text-danger">Erro ao carregar matrículas.</td></tr>';
    }
}

function irParaNotas() {
    window.location.href = `/nota?turma=${turmaId}`;
}

function irParaFrequencia() {
    window.location.href = `/frequencia?turma=${turmaId}`;
}
