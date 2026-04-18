//JS da nota
const h = () => ({ 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + localStorage.getItem('token') });

// Suporte para vir redirecionado do turma-detalhe.html
const urlParams = new URLSearchParams(window.location.search);
const initialTurmaId = urlParams.get('turma');

document.addEventListener('DOMContentLoaded', () => { 
    if (!localStorage.getItem('token')) { window.location.href='/login'; return; } 
    carregarDrops(); 
});

async function carregarDrops() {
    const [turmas, disc] = await Promise.all([
        (await fetch('/api/turmas?size=100&ativo=true', { headers: h() })).json(),
        (await fetch('/api/disciplinas?size=100&ativo=true', { headers: h() })).json()
    ]);
    
    document.getElementById('filtroTurma').innerHTML = '<option value="">Selecione a turma</option>' + 
        (turmas.content||[]).map(t => `<option value="${t.id}">Turma ${t.serie}º ${t.nome}</option>`).join('');
    document.getElementById('filtroDisciplina').innerHTML = '<option value="">Selecione a disciplina</option>' + 
        (disc.content||[]).map(d => `<option value="${d.id}">${d.descricao}</option>`).join('');

    if (initialTurmaId) {
        document.getElementById('filtroTurma').value = initialTurmaId;
    }
}

async function carregarNotas() {
    const turmaId = document.getElementById('filtroTurma').value;
    const disciplinaId = document.getElementById('filtroDisciplina').value;
    const container = document.getElementById('notasContainer');

    if (!turmaId || !disciplinaId) {
        container.innerHTML = '<div class="empty-state"><i class="bi bi-clipboard-data d-block"></i><p>Selecione Turma e Disciplina</p></div>';
        return;
    }

    container.innerHTML = '<div class="text-center py-4"><div class="spinner-border text-primary"></div></div>';

    try {
        const [alunosMatriculados, todasNotasTurmaDisciplina] = await Promise.all([
            (await fetch(`/api/matriculas/turma/${turmaId}`, { headers: h() })).json(),
            (await fetch(`/api/notas/turma/${turmaId}`, { headers: h() })).json() // Puxa todas as notas daquela turma globalmente
        ]);

        if (!alunosMatriculados.length) {
            container.innerHTML = '<div class="empty-state"><p>Nenhum aluno matriculado nesta turma</p></div>';
            return;
        }

        // Filter and map notas by Aluno and Periodo
        const notasDisciplinas = todasNotasTurmaDisciplina.filter(n => String(n.disciplinaId) === String(disciplinaId));
        
        const mapNotas = {};
        notasDisciplinas.forEach(n => {
            if (!mapNotas[n.alunoId]) mapNotas[n.alunoId] = {};
            // Vamos armazenar como BIM_1, BIM_2, BIM_3, BIM_4 e REC (anual ou de um periodo, mas o modelo só tem tipoAvaliacao. Vamos usar tipoAvaliacao = NOTA_BIM para periodos 1-4)
            if (n.tipoAvaliacao === 'REC') {
                mapNotas[n.alunoId]['REC'] = n;
            } else {
                mapNotas[n.alunoId][`BIM_${n.periodo}`] = n;
            }
        });

        let html = `<div class="table-responsive"><table class="table table-hover align-middle">
            <thead class="table-light text-center"><tr>
                <th class="text-start">Aluno</th>
                <th width="100">1º Bim</th><th width="100">2º Bim</th><th width="100">3º Bim</th><th width="100">4º Bim</th>
                <th width="100">Rec / Exame</th><th width="100">Média Final</th><th>Status</th>
            </tr></thead><tbody>`;

        alunosMatriculados.forEach(m => {
            const stuId = m.alunoId;
            const n = mapNotas[stuId] || {};
            
            const b1 = n.BIM_1 ? n.BIM_1.valor : ''; const id1 = n.BIM_1 ? n.BIM_1.id : '';
            const b2 = n.BIM_2 ? n.BIM_2.valor : ''; const id2 = n.BIM_2 ? n.BIM_2.id : '';
            const b3 = n.BIM_3 ? n.BIM_3.valor : ''; const id3 = n.BIM_3 ? n.BIM_3.id : '';
            const b4 = n.BIM_4 ? n.BIM_4.valor : ''; const id4 = n.BIM_4 ? n.BIM_4.id : '';
            const rec = n.REC ? n.REC.valor : '';   const idr = n.REC ? n.REC.id : '';

            // Calc media anual
            let mediaHtml = '-';
            let calc = 0;
            let aprovado = false;
            
            if (b1 !== '' && b2 !== '' && b3 !== '' && b4 !== '') {
                calc = (parseFloat(b1) + parseFloat(b2) + parseFloat(b3) + parseFloat(b4)) / 4;
                if (calc < 5 && rec !== '') {
                    calc = Math.max(calc, parseFloat(rec));
                }
                aprovado = calc >= 5;
                mediaHtml = calc.toFixed(1);
            }

            const statusHtml = mediaHtml === '-' ? '<span class="badge bg-secondary">Pendente</span>' : 
                               aprovado ? '<span class="badge bg-success">Aprovado</span>' : '<span class="badge bg-danger">Rec/Reprovado</span>';

            html += `<tr>
                <td class="fw-bold text-start"><small class="text-muted d-block">${m.alunoRa}</small>${m.alunoNome}</td>
                <td><input type="number" step="0.1" min="0" max="10" class="form-control form-control-sm text-center" value="${b1}" 
                     onblur="salvarNota(this, ${stuId}, 1, 'NOTA_BIM', '${id1}')"></td>
                <td><input type="number" step="0.1" min="0" max="10" class="form-control form-control-sm text-center" value="${b2}" 
                     onblur="salvarNota(this, ${stuId}, 2, 'NOTA_BIM', '${id2}')"></td>
                <td><input type="number" step="0.1" min="0" max="10" class="form-control form-control-sm text-center" value="${b3}" 
                     onblur="salvarNota(this, ${stuId}, 3, 'NOTA_BIM', '${id3}')"></td>
                <td><input type="number" step="0.1" min="0" max="10" class="form-control form-control-sm text-center" value="${b4}" 
                     onblur="salvarNota(this, ${stuId}, 4, 'NOTA_BIM', '${id4}')"></td>
                
                <td class="bg-light border-start"><input type="number" step="0.1" min="0" max="10" class="form-control form-control-sm text-center border-warning" value="${rec}" 
                     onblur="salvarNota(this, ${stuId}, 4, 'REC', '${idr}')" placeholder="-"></td>
                
                <td class="text-center"><strong class="${aprovado ? 'text-success' : (mediaHtml!=='-'?'text-danger':'')}">${mediaHtml}</strong></td>
                <td class="text-center">${statusHtml}</td>
            </tr>`;
        });

        html += `</tbody></table></div>
        <div class="alert alert-info py-2 mt-3 mb-0" style="font-size: 13px">
            <i class="bi bi-info-circle me-2"></i>As avaliações ocorrem por bimestre (1º, 2º, 3º e 4º). Digite a nota do aluno na respectiva coluna e ela será salva automaticamente.
        </div>`;
        container.innerHTML = html;

    } catch (e) {
        console.error(e);
        container.innerHTML = `<div class="alert alert-danger">Erro ao carregar dados.</div>`;
    }
}

async function salvarNota(inputNode, alunoId, periodoInt, tipoAvaliacao, notaIdOriginal) {
    const valor = inputNode.value;
    if (valor === '') return;
    
    let num = parseFloat(valor);
    if (num < 0) num = 0; if (num > 10) num = 10;
    inputNode.value = num.toFixed(1);

    const turmaId = document.getElementById('filtroTurma').value;
    const disciplinaId = document.getElementById('filtroDisciplina').value;

    inputNode.style.borderColor = 'orange';

    try {
        if (notaIdOriginal && notaIdOriginal !== 'undefined' && notaIdOriginal !== '') {
            // Atualização simples via PUT — ID já é conhecido, não precisa recarregar
            await fetch(`/api/notas/${notaIdOriginal}`, {
                method: 'PUT', headers: h(), body: JSON.stringify({ valor: num })
            });
            inputNode.style.borderColor = '#27ae60';
            inputNode.style.backgroundColor = '#eafaf1';
            setTimeout(() => { inputNode.style.borderColor = ''; inputNode.style.backgroundColor = ''; }, 800);
        } else {
            // Nova nota via POST — após salvar, buscamos o ID gerado e atualizamos
            // apenas o atributo onblur deste input, sem reconstruir a tabela inteira.
            const res = await fetch('/api/notas', {
                method: 'POST', headers: h(), body: JSON.stringify({
                    turmaId: turmaId, disciplinaId: disciplinaId,
                    alunoId: alunoId, periodo: periodoInt,
                    tipoAvaliacao: tipoAvaliacao, valor: num
                })
            });
            const novaNota = res.ok ? await res.json() : null;
            if (novaNota && novaNota.id) {
                // Atualiza o onblur do input com o novo ID para que futuras
                // edições usem PUT em vez de POST, sem recarregar a tabela.
                inputNode.setAttribute('onblur',
                    `salvarNota(this, ${alunoId}, ${periodoInt}, '${tipoAvaliacao}', '${novaNota.id}')`);
            }
            inputNode.style.borderColor = '#27ae60';
            inputNode.style.backgroundColor = '#eafaf1';
            setTimeout(() => { inputNode.style.borderColor = ''; inputNode.style.backgroundColor = ''; }, 800);
        }

        // Atualiza a coluna Média Final e Status da linha afetada sem recarregar a tabela
        atualizarMediaLinha(inputNode);

    } catch (e) {
        inputNode.style.borderColor = 'red';
        console.error(e);
        alert('Falha ao salvar nota.');
    }
}

/**
 * Recalcula Média Final e Status da linha do input modificado,
 * sem reconstruir o HTML da tabela.
 */
function atualizarMediaLinha(inputNode) {
    const tr = inputNode.closest('tr');
    if (!tr) return;

    const inputs = tr.querySelectorAll('input[type="number"]');
    // Ordem: BIM1, BIM2, BIM3, BIM4, REC
    const vals = Array.from(inputs).map(i => i.value !== '' ? parseFloat(i.value) : null);
    const [b1, b2, b3, b4, rec] = vals;

    const tdMedia = tr.cells[tr.cells.length - 2];
    const tdStatus = tr.cells[tr.cells.length - 1];
    if (!tdMedia || !tdStatus) return;

    if (b1 !== null && b2 !== null && b3 !== null && b4 !== null) {
        let calc = (b1 + b2 + b3 + b4) / 4;
        if (calc < 5 && rec !== null) calc = Math.max(calc, rec);
        const aprovado = calc >= 5;
        tdMedia.innerHTML = `<strong class="${aprovado ? 'text-success' : 'text-danger'}">${calc.toFixed(1)}</strong>`;
        tdStatus.innerHTML = aprovado
            ? '<span class="badge bg-success">Aprovado</span>'
            : '<span class="badge bg-danger">Rec/Reprovado</span>';
    } else {
        tdMedia.innerHTML = '<strong>-</strong>';
        tdStatus.innerHTML = '<span class="badge bg-secondary">Pendente</span>';
    }
}
