//JS dos relatórios
const h = () => ({ 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + localStorage.getItem('token') });

let listAlunos = [];
let alunosMap = {};

document.addEventListener('DOMContentLoaded', () => { 
    if (!localStorage.getItem('token')) { window.location.href='/login'; return; } 
    carregarAlunos(); 
});

async function carregarAlunos() {
    try {
        const data = await (await fetch('/api/alunos?page=0&size=500', { headers: h() })).json();
        listAlunos = data.content || [];
        const datalist = document.getElementById('listaAlunos');
        let html = '';
        listAlunos.forEach(a => {
            alunosMap[`${a.ra} - ${a.nome}`] = a;
            html += `<option value="${a.ra} - ${a.nome}">`;
        });
        datalist.innerHTML = html;
    } catch (e) { console.error('Erro ao carregar lista para o datalist:', e); }
}

function limparTela() {
    document.getElementById('printArea').classList.add('d-none');
}

function calcularMediaPeriodo(notasObj, periodo) {
    const p = notasObj[periodo];
    if (!p) return { v1: '-', v2: '-', rec: '-', calc: '-' };
    const av1 = p['AV1'] ? p['AV1'].valor : '-';
    const av2 = p['AV2'] ? p['AV2'].valor : '-';
    const rec = p['REC'] ? p['REC'].valor : '-';
    let calc = '-';
    
    if (av1 !== '-' && av2 !== '-') {
        let m = (parseFloat(av1) + parseFloat(av2)) / 2;
        if (m < 5 && rec !== '-') {
            m = Math.max(m, parseFloat(rec));
        }
        calc = m.toFixed(1);
    }
    return { v1: av1, v2: av2, rec, calc };
}

async function gerarBoletim() {
    const val = document.getElementById('filtroAluno').value;
    const ano = document.getElementById('filtroAno').value;
    const aluno = alunosMap[val];

    if (!aluno) { alert('Por favor, selecione um aluno válido da lista.'); return; }

    document.getElementById('loadingIndicator').classList.remove('d-none');
    document.getElementById('printArea').classList.add('d-none');

    try {
        // Encontrar turmas e matriculas do aluno
        const mats = await (await fetch(`/api/matriculas/aluno/${aluno.id}`, { headers: h() })).json();
        const mat = mats.find(m => String(m.anoLetivo) === ano && m.situacao === 'ATIVA');
        
        let turmaNome = 'Sem Matrícula';
        let presencaGlobal = '100%';
        let freqTotal = 0, freqP = 0, freqF = 0, freqJ = 0;

        if (mat) {
            turmaNome = `Turma ${mat.serie}º ${mat.turmaTurno} (Cod: ${mat.turmaId})`; 
            // no MVP calculamos presenca global por fetch das frequencias global
            const freqs = await (await fetch(`/api/frequencias/aluno/${aluno.id}`, { headers: h() })).json();
            
            // Filter frequencias only from this class if possible, or all if global
            freqTotal = freqs.length;
            freqs.forEach(f => {
                if (f.status === 'PRESENTE') freqP++;
                else if (f.status === 'AUSENTE') freqF++;
                else if (f.status === 'JUSTIFICADO') freqJ++;
            });

            if (freqTotal > 0) {
                presencaGlobal = (((freqP + freqJ) / freqTotal) * 100).toFixed(1) + '%';
            } else {
                presencaGlobal = '-';
            }
        }

        // Fetch das Notas
        const todasNotas = await (await fetch(`/api/notas/aluno/${aluno.id}`, { headers: h() })).json();
        
        // Agrupar disciplinas independentemente se ele tem turma
        // Map: { disciplinaId: { descricao: 'Matematica', notas: { 1: { AV1: x, AV2: y}, 2: {...} } } }
        
        // Let's get all disciplinas to list them even if they have no grades
        const discAPI = await (await fetch('/api/disciplinas?size=100', { headers: h() })).json();
        const disciplinas = discAPI.content || [];
        
        const discMap = {};
        disciplinas.forEach(d => {
            discMap[d.id] = { nome: d.descricao, notas: { 1:{}, 2:{}, 3:{}, 4:{} } };
        });

        todasNotas.forEach(n => {
            if (String(n.turmaId) !== String((mat||{}).turmaId)) return; // isolar por ano/turma
            if (discMap[n.disciplinaId]) {
                discMap[n.disciplinaId].notas[n.periodo][n.tipoAvaliacao] = n;
            }
        });

        // Montar a View
        document.getElementById('lblAno').textContent = ano;
        document.getElementById('lblNome').textContent = aluno.nome;
        document.getElementById('lblRa').textContent = aluno.ra;
        document.getElementById('lblResp').textContent = aluno.nomeResponsavel || 'Não cadastrado';
        document.getElementById('lblTurma').textContent = turmaNome;
        document.getElementById('lblFreq').textContent = presencaGlobal;

        document.getElementById('lblFreqTotal').textContent = freqTotal;
        document.getElementById('lblFreqPresenca').textContent = freqP;
        document.getElementById('lblFreqFalta').textContent = freqF;
        document.getElementById('lblFreqJustificada').textContent = freqJ;

        let tbody = '';
        let dependencias = 0;

        Object.values(discMap).forEach(d => {
            const t1 = calcularMediaPeriodo(d.notas, 1);
            const t2 = calcularMediaPeriodo(d.notas, 2);
            const t3 = calcularMediaPeriodo(d.notas, 3);
            const t4 = calcularMediaPeriodo(d.notas, 4);

            let mediaAnual = '-';
            let aprovadoStatus = '-';
            
            if (t1.calc !== '-' && t2.calc !== '-' && t3.calc !== '-' && t4.calc !== '-') {
                const mAnual = (parseFloat(t1.calc) + parseFloat(t2.calc) + parseFloat(t3.calc) + parseFloat(t4.calc)) / 4;
                mediaAnual = mAnual.toFixed(1);
                if (mAnual >= 5) aprovadoStatus = '<span class="text-success fw-bold">AP</span>';
                else { aprovadoStatus = '<span class="text-danger fw-bold">RP</span>'; dependencias++; }
            }

            tbody += `<tr>
                <td class="fw-bold text-start">${d.nome}</td>
                <td class="text-muted small">${t1.v1}</td><td class="text-muted small">${t1.v2}</td><td class="bg-warning bg-opacity-10 fw-bold">${t1.calc}</td>
                <td class="text-muted small">${t2.v1}</td><td class="text-muted small">${t2.v2}</td><td class="bg-warning bg-opacity-10 fw-bold">${t2.calc}</td>
                <td class="text-muted small">${t3.v1}</td><td class="text-muted small">${t3.v2}</td><td class="bg-warning bg-opacity-10 fw-bold">${t3.calc}</td>
                <td class="text-muted small">${t4.v1}</td><td class="text-muted small">${t4.v2}</td><td class="bg-warning bg-opacity-10 fw-bold">${t4.calc}</td>
                <td class="fw-bold bg-light fs-6 ${mediaAnual!=='-' && parseFloat(mediaAnual)<5 ? 'text-danger' : 'text-primary'}">${mediaAnual}</td>
                <td>${aprovadoStatus}</td>
            </tr>`;
        });

        document.getElementById('boletimBody').innerHTML = tbody;

        let finalSit = '';
        if (mat) {
             if (dependencias === 0) { finalSit = '<span class="badge bg-success">Aprovado por Notas</span>'; }
             else { finalSit = ` <span class="badge bg-danger">Em Recuperação (${dependencias} disc.)</span>`; }
        } else {
             finalSit = '<span class="badge bg-secondary">Sem turma</span>';
        }
        document.getElementById('lblSit').innerHTML = finalSit;

        document.getElementById('loadingIndicator').classList.add('d-none');
        document.getElementById('printArea').classList.remove('d-none');

    } catch (e) {
        console.error(e);
        alert('Erro ao processar as notas e matrículas do aluno.');
        document.getElementById('loadingIndicator').classList.add('d-none');
    }
}
