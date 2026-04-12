//JS da frequencia

const h = () => ({ 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + localStorage.getItem('token') });

// Suporte para vir redirecionado do turma-detalhe.html
const urlParams = new URLSearchParams(window.location.search);
const initialTurmaId = urlParams.get('turma');

document.addEventListener('DOMContentLoaded', () => {
    if (!localStorage.getItem('token')) { window.location.href='/login'; return; }
    document.getElementById('filtroData').value = new Date().toISOString().split('T')[0];
    carregarTurmas();
});

async function carregarTurmas() {
    const data = await (await fetch('/api/turmas?size=100&ativo=true', { headers: h() })).json();
    document.getElementById('filtroTurma').innerHTML = '<option value="">Selecione a turma</option>' +
        (data.content||[]).map(t => `<option value="${t.id}">Turma ${t.serie}º ${t.nome}</option>`).join('');
    
    if (initialTurmaId) {
        document.getElementById('filtroTurma').value = initialTurmaId;
        carregarFrequencia();
    }
}

async function carregarFrequencia() {
    const turmaId = document.getElementById('filtroTurma').value;
    const data = document.getElementById('filtroData').value;
    const container = document.getElementById('freqContainer');
    
    if (!turmaId || !data) {
        container.innerHTML = '<div class="empty-state"><i class="bi bi-calendar-check d-block"></i><p>Selecione uma turma e data</p></div>';
        return;
    }

    container.innerHTML = '<div class="text-center py-4"><div class="spinner-border text-primary"></div></div>';

    try {
        const [mats, freqs] = await Promise.all([
            (await fetch(`/api/matriculas/turma/${turmaId}`, { headers: h() })).json(),
            // Frequência global para o dia - usando disciplina genérica 1 para MVP
            (await fetch(`/api/frequencias/turma/${turmaId}/disciplina/1?data=${data}`, { headers: h() })).json()
        ]);

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
        container.innerHTML = `<div class="alert alert-danger">Erro ao carregar dados da frequência.</div>`;
    }
}

async function salvarFreq(alunoId, statusInput, originalId) {
    const turmaId = document.getElementById('filtroTurma').value;
    const data = document.getElementById('filtroData').value;
    const tdStatus = document.getElementById(`td_status_${alunoId}`);
    
    tdStatus.innerHTML = '<span class="spinner-border spinner-border-sm text-primary"></span>';

    try {
        if (originalId && originalId !== 'undefined' && originalId !== '') {
            await fetch(`/api/frequencias/${originalId}`, {
                method: 'PUT', headers: h(), body: JSON.stringify({
                    turmaId: parseInt(turmaId), disciplinaId: 1, alunoId, data, status: statusInput
                })
            });
        } else {
            await fetch('/api/frequencias', {
                method: 'POST', headers: h(), body: JSON.stringify({
                    turmaId: parseInt(turmaId), disciplinaId: 1, alunoId, data, status: statusInput
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
        tdStatus.innerHTML = '<span class="badge bg-danger">Erro</span>';
        alert('Falha ao salvar a frequência');
    }
}
