//JS do Index
const API = '/api';

document.addEventListener('DOMContentLoaded', async () => {
    if (!localStorage.getItem('token')) { window.location.href = '/login'; return; }
    try {
        const [alunos, profs, turmas, discs] = await Promise.all([
            apiFetch(`${API}/alunos?size=1`).then(r => r.json()),
            apiFetch(`${API}/professores?size=1&ativo=true`).then(r => r.json()),
            apiFetch(`${API}/turmas?anoLetivo=2026&size=1`).then(r => r.json()),
            apiFetch(`${API}/disciplinas?size=1`).then(r => r.json())
        ]);
        document.getElementById('totalAlunos').textContent = alunos.totalElements || 0;
        document.getElementById('totalProfessores').textContent = profs.totalElements || 0;
        document.getElementById('totalTurmas').textContent = turmas.totalElements || 0;
        document.getElementById('totalDisciplinas').textContent = discs.totalElements || 0;
    } catch(e) { console.error(e); }

    // Charts: Matrículas (Alunos por série) em vez de apenas turmas
    try {
        const matsRes = await apiFetch(`${API}/matriculas?size=1000`).then(r => r.json());
        const matsList = matsRes.content || [];
        
        const seriesCount = [0,0,0,0,0];
        const statusCount = { ATIVA: 0, TRANCADA: 0, CANCELADA: 0, CONCLUIDA: 0 };
        
        matsList.forEach(m => {
            // Count current year's active students by series
            if (m.anoLetivo === 2026 && m.serie >= 1 && m.serie <= 5) {
                seriesCount[m.serie - 1]++;
            }
            if (m.anoLetivo === 2026 && statusCount[m.situacao] !== undefined) {
                statusCount[m.situacao]++;
            }
        });

        new Chart(document.getElementById('graficoSeries'), {
            type: 'bar',
            data: {
                labels: ['1º Ano', '2º Ano', '3º Ano', '4º Ano', '5º Ano'],
                datasets: [{ 
                    label: 'Alunos Matriculados (2026)', 
                    data: seriesCount,
                    backgroundColor: ['#3498db', '#2ecc71', '#f39c12', '#e74c3c', '#9b59b6'],
                    borderRadius: 8, borderSkipped: false 
                }]
            },
            options: { 
                responsive: true, 
                plugins: { legend: { display: false } },
                scales: { y: { beginAtZero: true, ticks: { stepSize: 5 } } } 
            }
        });

        new Chart(document.getElementById('graficoTurnos'), {
            type: 'doughnut',
            data: {
                labels: ['Ativas', 'Trancadas', 'Canceladas', 'Concluídas'],
                datasets: [{ 
                    data: [statusCount.ATIVA, statusCount.TRANCADA, statusCount.CANCELADA, statusCount.CONCLUIDA],
                    backgroundColor: ['#2ecc71', '#f39c12', '#e74c3c', '#3498db'], 
                    borderWidth: 0 
                }]
            },
            options: { responsive: true, cutout: '70%' }
        });
    } catch(e) { console.error(e); }
});
