//JS da frequencia

// Suporte para vir redirecionado do turma-detalhe.html
const urlParams = new URLSearchParams(window.location.search);
const initialTurmaId = urlParams.get('turma');

document.addEventListener('DOMContentLoaded', () => {
    if (!localStorage.getItem('token')) { window.location.href='/login'; return; }
    document.getElementById('filtroData').value = new Date().toISOString().split('T')[0];
    carregarTurmas();
});

async function carregarTurmas() {
    const data = await (await apiFetch('/api/turmas?size=100&ativo=true')).json();
    document.getElementById('filtroTurma').innerHTML = '<option value="">Selecione a turma</option>' +
        (data.content||[]).map(t => `<option value="${t.id}">Turma ${t.serie}º ${t.nome}</option>`).join('');
    
    if (initialTurmaId) {
        document.getElementById('filtroTurma').value = initialTurmaId;
        carregarGradeEAtualizarUI();
    }
}

let gradeAtual = [];

const diasDaSemanaJava = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];

async function carregarGradeEAtualizarUI() {
    const turmaId = document.getElementById('filtroTurma').value;
    const dataVal = document.getElementById('filtroData').value;
    const filtroAula = document.getElementById('filtroAula');
    const container = document.getElementById('freqContainer');
    
    filtroAula.style.display = 'none';
    filtroAula.innerHTML = '<option value="">Selecione a Aula</option>';
    container.innerHTML = '<div class="empty-state"><i class="bi bi-calendar-check d-block"></i><p>Selecione uma turma, data e aula</p></div>';

    if (!turmaId || !dataVal) return;

    try {
        // Passa o login do professor para filtrar apenas suas disciplinas
        const login = localStorage.getItem('login') || '';
        const role = localStorage.getItem('role') || '';
        const loginParam = role === 'PROFESSOR' ? `&login=${encodeURIComponent(login)}` : '';
        
        const res = await apiFetch(`/api/turmas/${turmaId}/grade?dummy=1${loginParam}`);
        if (!res.ok) throw new Error();
        gradeAtual = await res.json();
        
        // Determinar o dia da semana
        const [y, m, d] = dataVal.split('-');
        const dateObj = new Date(y, m - 1, d);
        const jsDay = dateObj.getDay();
        const javaDay = diasDaSemanaJava[jsDay];
        
        const aulasDoDia = gradeAtual.filter(g => g.diaSemana === javaDay).sort((a,b) => a.numeroAula - b.numeroAula);
        
        if (aulasDoDia.length === 0) {
            container.innerHTML = '<div class="alert alert-warning">Não há aulas para esta turma neste dia' + (role === 'PROFESSOR' ? ' nas suas disciplinas' : '') + '.</div>';
            return;
        }
        
        aulasDoDia.forEach(aula => {
            // Usa disciplinaId e disciplinaDescricao do novo GradeHorariaResponse DTO
            filtroAula.innerHTML += `<option value="${aula.numeroAula}" data-disciplina-id="${aula.disciplinaId}">Aula ${aula.numeroAula} — ${aula.disciplinaDescricao}</option>`;
        });
        
        filtroAula.style.display = 'block';
    } catch (e) {
        console.error(e);
        container.innerHTML = '<div class="alert alert-danger">Erro ao carregar a grade horária da turma.</div>';
    }
}

async function carregarFrequencia() {
    const turmaId = document.getElementById('filtroTurma').value;
    const data = document.getElementById('filtroData').value;
    const filtroAula = document.getElementById('filtroAula');
    const numeroAula = filtroAula.value;
    const container = document.getElementById('freqContainer');
    
    if (!turmaId || !data || !numeroAula) {
        container.innerHTML = '<div class="empty-state"><i class="bi bi-calendar-check d-block"></i><p>Selecione uma turma, data e aula</p></div>';
        return;
    }

    const selectedOption = filtroAula.options[filtroAula.selectedIndex];
    const disciplinaId = selectedOption.getAttribute('data-disciplina-id');

    container.innerHTML = '<div class="text-center py-4"><div class="spinner-border text-primary"></div></div>';

    try {
        const [mats, freqsRes] = await Promise.all([
            apiFetch(`/api/matriculas/turma/${turmaId}`).then(r => r.json()),
            apiFetch(`/api/frequencias/turma/${turmaId}/disciplina/${disciplinaId}?data=${data}`).then(r => r.json())
        ]);
        
        // A API de listar pode trazer de todas as aulas daquele dia, precisamos filtrar para a aula específica
        const freqs = freqsRes.filter(f => f.numeroAula == numeroAula);

        if (!mats.length) { 
            container.innerHTML = '<div class="empty-state"><p>Nenhum aluno matriculado nesta turma</p></div>'; 
            return; 
        }

        const byAluno = {};
        freqs.forEach(f => { byAluno[f.alunoId] = f; });

        let html = `<table class="table table-hover align-middle">
            <thead class="table-light"><tr><th>RA</th><th>Aluno</th><th>Situação Diária</th><th>Ações Rápida</th></tr></thead><tbody>`;

        mats.forEach(m => {
            const stuId = m.alunoId;
            const freq = byAluno[stuId];
            const status = freq ? freq.status : '';
            const fId = freq ? freq.id : '';

            html += `<tr>
                <td class="text-muted small">${m.alunoRa}</td>
                <td class="fw-bold">${m.alunoNome}</td>
                <td id="td_status_${stuId}">
                    ${status === 'PRESENTE' ? '<span class="badge bg-success">Presente</span>' : ''}
                    ${status === 'AUSENTE' ? '<span class="badge bg-danger">Ausente</span>' : ''}
                    ${status === 'JUSTIFICADO' ? '<span class="badge bg-warning text-dark">Justificado</span>' : ''}
                    ${!status ? '<span class="badge bg-secondary">Não Registrada</span>' : ''}
                </td>
                <td>
                    <div class="btn-group" role="group">
                        <input type="radio" class="btn-check" name="freq_${stuId}" id="p_${stuId}" autocomplete="off" 
                            onchange="salvarFreq(${stuId}, 'PRESENTE', '${fId}')" ${status==='PRESENTE'?'checked':''}>
                        <label class="btn btn-outline-success btn-sm px-3" for="p_${stuId}">P</label>
                        
                        <input type="radio" class="btn-check" name="freq_${stuId}" id="a_${stuId}" autocomplete="off" 
                            onchange="salvarFreq(${stuId}, 'AUSENTE', '${fId}')" ${status==='AUSENTE'?'checked':''}>
                        <label class="btn btn-outline-danger btn-sm px-3" for="a_${stuId}">F</label>

                        <input type="radio" class="btn-check" name="freq_${stuId}" id="j_${stuId}" autocomplete="off" 
                            onchange="salvarFreq(${stuId}, 'JUSTIFICADO', '${fId}')" ${status==='JUSTIFICADO'?'checked':''}>
                        <label class="btn btn-outline-warning btn-sm px-3" for="j_${stuId}">J</label>
                    </div>
                </td>
            </tr>`;
        });

        html += `</tbody></table>
        <div class="alert alert-info py-2 mt-3 mb-0" style="font-size: 13px">
            <i class="bi bi-info-circle me-2"></i>As presenças e faltas são salvas logo ao clicar no botão.
        </div>`;
        
        container.innerHTML = html;

    } catch (e) {
        console.error(e);
        if (e.message && e.message.includes('expirada')) return;
        container.innerHTML = `<div class="alert alert-danger">Erro ao carregar dados da frequência.</div>`;
    }
}

async function salvarFreq(alunoId, statusInput, originalId) {
    const turmaId = document.getElementById('filtroTurma').value;
    const data = document.getElementById('filtroData').value;
    const filtroAula = document.getElementById('filtroAula');
    const numeroAula = parseInt(filtroAula.value);
    const disciplinaId = parseInt(filtroAula.options[filtroAula.selectedIndex].getAttribute('data-disciplina-id'));
    const tdStatus = document.getElementById(`td_status_${alunoId}`);
    
    tdStatus.innerHTML = '<span class="spinner-border spinner-border-sm text-primary"></span>';

    try {
        if (originalId && originalId !== 'undefined' && originalId !== '') {
            await apiFetch(`/api/frequencias/${originalId}`, {
                method: 'PUT', body: JSON.stringify({
                    turmaId: parseInt(turmaId), disciplinaId, alunoId, data, numeroAula, status: statusInput
                })
            });
        } else {
            await apiFetch('/api/frequencias', {
                method: 'POST', body: JSON.stringify({
                    turmaId: parseInt(turmaId), disciplinaId, alunoId, data, numeroAula, status: statusInput
                })
            });
        }
        
        // Update tag visually without full reload
        let badge = '';
        if (statusInput === 'PRESENTE') badge = '<span class="badge bg-success">Presente</span>';
        if (statusInput === 'AUSENTE') badge = '<span class="badge bg-danger">Ausente</span>';
        if (statusInput === 'JUSTIFICADO') badge = '<span class="badge bg-warning text-dark">Justificado</span>';
        tdStatus.innerHTML = badge;

        // Fetch to renew the binding ID so consecutive clicks don't crash
        setTimeout(carregarFrequencia, 800);

    } catch (e) {
        if (e.message && e.message.includes('expirada')) return;
        tdStatus.innerHTML = '<span class="badge bg-danger">Erro</span>';
        alert('Falha ao salvar a frequência');
    }
}
