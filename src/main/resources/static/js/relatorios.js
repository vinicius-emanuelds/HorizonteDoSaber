//JS dos relatórios
const h = () => ({
  "Content-Type": "application/json",
  Authorization: "Bearer " + localStorage.getItem("token"),
});

let listAlunos = [];
let alunosMap = {};

document.addEventListener("DOMContentLoaded", () => {
  if (!localStorage.getItem("token")) {
    window.location.href = "/login";
    return;
  }
  carregarAlunos();
});

async function carregarAlunos() {
  try {
    const data = await (
      await fetch("/api/alunos?page=0&size=500", { headers: h() })
    ).json();
    listAlunos = data.content || [];
    const datalist = document.getElementById("listaAlunos");
    let html = "";
    listAlunos.forEach((a) => {
      alunosMap[`${a.ra} - ${a.nome}`] = a;
      html += `<option value="${a.ra} - ${a.nome}">`;
    });
    datalist.innerHTML = html;
  } catch (e) {
    console.error("Erro ao carregar lista para o datalist:", e);
  }
}

function limparTela() {
  document.getElementById("printArea").classList.add("d-none");
}

function calcularMediaPeriodo(notasObj, periodo) {
  const p = notasObj[periodo];
  if (!p) return { v1: "-", v2: "-", rec: "-", calc: "-" };
  const av1 = p["AV1"] ? p["AV1"].valor : "-";
  const av2 = p["AV2"] ? p["AV2"].valor : "-";
  const rec = p["REC"] ? p["REC"].valor : "-";
  let calc = "-";

  if (av1 !== "-" && av2 !== "-") {
    let m = (parseFloat(av1) + parseFloat(av2)) / 2;
    if (m < 5 && rec !== "-") {
      m = Math.max(m, parseFloat(rec));
    }
    calc = m.toFixed(1);
  }
  return { v1: av1, v2: av2, rec, calc };
}

async function gerarBoletim() {
  const val = document.getElementById("filtroAluno").value;
  const ano = document.getElementById("filtroAno").value;
  const aluno = alunosMap[val];

  if (!aluno) {
    alert("Por favor, selecione um aluno válido da lista.");
    return;
  }

  document.getElementById("loadingIndicator").classList.remove("d-none");
  document.getElementById("printArea").classList.add("d-none");

  try {
    // Encontrar turmas e matriculas do aluno
    const mats = await (
      await fetch(`/api/matriculas/aluno/${aluno.id}`, { headers: h() })
    ).json();
    const mat = mats.find(
      (m) => String(m.anoLetivo) === ano && m.situacao === "ATIVA",
    );

    let turmaNome = "Sem Matrícula";
    let presencaGlobal = "100%";
    let freqTotal = 0,
      freqP = 0,
      freqF = 0,
      freqJ = 0;

    if (mat) {
      turmaNome = `Turma ${mat.serie}º ${mat.turmaTurno} (Cod: ${mat.turmaId})`;
      // no MVP calculamos presenca global por fetch das frequencias global
      const freqs = await (
        await fetch(`/api/frequencias/aluno/${aluno.id}`, { headers: h() })
      ).json();

      // Filter frequencias only from this class if possible, or all if global
      freqTotal = freqs.length;
      freqs.forEach((f) => {
        if (f.status === "PRESENTE") freqP++;
        else if (f.status === "AUSENTE") freqF++;
        else if (f.status === "JUSTIFICADO") freqJ++;
      });

      if (freqTotal > 0) {
        presencaGlobal = (((freqP + freqJ) / freqTotal) * 100).toFixed(1) + "%";
      } else {
        presencaGlobal = "-";
      }
    }

    // Fetch das Notas
    const todasNotas = await (
      await fetch(`/api/notas/aluno/${aluno.id}`, { headers: h() })
    ).json();

    // Agrupar disciplinas independentemente se ele tem turma
    // Map: { disciplinaId: { descricao: 'Matematica', notas: { 1: { AV1: x, AV2: y}, 2: {...} } } }

    // Let's get all disciplinas to list them even if they have no grades
    const discAPI = await (
      await fetch("/api/disciplinas?size=100", { headers: h() })
    ).json();
    const disciplinas = discAPI.content || [];

    const discMap = {};
    disciplinas.forEach((d) => {
      discMap[d.id] = {
        nome: d.descricao,
        notas: { 1: {}, 2: {}, 3: {}, 4: {} },
      };
    });

    todasNotas.forEach((n) => {
      if (String(n.turmaId) !== String((mat || {}).turmaId)) return; // isolar por ano/turma
      if (discMap[n.disciplinaId]) {
        discMap[n.disciplinaId].notas[n.periodo][n.tipoAvaliacao] = n;
      }
    });

    // Montar a View
    document.getElementById("lblAno").textContent = ano;
    document.getElementById("lblNome").textContent = aluno.nome;
    document.getElementById("lblRa").textContent = aluno.ra;
    document.getElementById("lblResp").textContent =
      aluno.nomeResponsavel || "Não cadastrado";
    document.getElementById("lblTurma").textContent = turmaNome;
    document.getElementById("lblFreq").textContent = presencaGlobal;

    document.getElementById("lblFreqTotal").textContent = freqTotal;
    document.getElementById("lblFreqPresenca").textContent = freqP;
    document.getElementById("lblFreqFalta").textContent = freqF;
    document.getElementById("lblFreqJustificada").textContent = freqJ;

    let tbody = "";
    let dependencias = 0;

    Object.values(discMap).forEach((d) => {
      const t1 = calcularMediaPeriodo(d.notas, 1);
      const t2 = calcularMediaPeriodo(d.notas, 2);
      const t3 = calcularMediaPeriodo(d.notas, 3);
      const t4 = calcularMediaPeriodo(d.notas, 4);

      let mediaAnual = "-";
      let aprovadoStatus = "-";

      if (
        t1.calc !== "-" &&
        t2.calc !== "-" &&
        t3.calc !== "-" &&
        t4.calc !== "-"
      ) {
        const mAnual =
          (parseFloat(t1.calc) +
            parseFloat(t2.calc) +
            parseFloat(t3.calc) +
            parseFloat(t4.calc)) /
          4;
        mediaAnual = mAnual.toFixed(1);
        if (mAnual >= 5)
          aprovadoStatus = '<span class="text-success fw-bold">AP</span>';
        else {
          aprovadoStatus = '<span class="text-danger fw-bold">RP</span>';
          dependencias++;
        }
      }

      tbody += `<tr>
    <td class="fw-bold text-start" style="position:relative;">
        ${d.nome}
        <span style="position:absolute;right:14px;top:50%;transform:translateY(-50%);">
            ${aprovadoStatus}
        </span>
    </td>

    <!-- Grid mobile dos bimestres (visível só no mobile via CSS) -->
    <td colspan="12" class="p-0 d-none d-md-none bim-mobile-cell">
        <div style="display:grid;grid-template-columns:repeat(4,1fr);border-bottom:1px solid #dee2e6;">
            ${[
              { label: "1º Bim", d: t1 },
              { label: "2º Bim", d: t2 },
              { label: "3º Bim", d: t3 },
              { label: "4º Bim", d: t4 },
            ]
              .map(
                (b) => `
                <div style="padding:8px 10px;border-right:1px solid #dee2e6;">
                    <div style="font-size:10px;color:#999;margin-bottom:4px;">${b.label}</div>
                    <div style="display:flex;gap:4px;margin-bottom:4px;">
                        <span style="font-size:11px;background:#f0f4f8;border-radius:4px;padding:1px 5px;">${b.d.v1}</span>
                        <span style="font-size:11px;background:#f0f4f8;border-radius:4px;padding:1px 5px;">${b.d.v2}</span>
                    </div>
                    <div style="font-size:13px;font-weight:600;color:${b.d.calc !== "-" && parseFloat(b.d.calc) < 5 ? "#e74c3c" : "#2c3e50"}">
                        ${b.d.calc}
                    </div>
                </div>
            `,
              )
              .join("")}
        </div>
        <div style="display:flex;justify-content:space-between;align-items:center;padding:8px 14px;">
            <span style="font-size:12px;color:#7f8c8d;">Média final</span>
            <span style="font-size:18px;font-weight:700;color:${mediaAnual !== "-" && parseFloat(mediaAnual) < 5 ? "#e74c3c" : "#1a5276"}">
                ${mediaAnual}
            </span>
        </div>
    </td>

    <!-- Colunas originais (visíveis só no desktop/impressão) -->
    <td class="text-muted small d-none d-md-table-cell">${t1.v1}</td>
    <td class="text-muted small d-none d-md-table-cell">${t1.v2}</td>
    <td class="bg-warning bg-opacity-10 fw-bold d-none d-md-table-cell">${t1.calc}</td>
    <td class="text-muted small d-none d-md-table-cell">${t2.v1}</td>
    <td class="text-muted small d-none d-md-table-cell">${t2.v2}</td>
    <td class="bg-warning bg-opacity-10 fw-bold d-none d-md-table-cell">${t2.calc}</td>
    <td class="text-muted small d-none d-md-table-cell">${t3.v1}</td>
    <td class="text-muted small d-none d-md-table-cell">${t3.v2}</td>
    <td class="bg-warning bg-opacity-10 fw-bold d-none d-md-table-cell">${t3.calc}</td>
    <td class="text-muted small d-none d-md-table-cell">${t4.v1}</td>
    <td class="text-muted small d-none d-md-table-cell">${t4.v2}</td>
    <td class="bg-warning bg-opacity-10 fw-bold d-none d-md-table-cell">${t4.calc}</td>
    <td class="fw-bold bg-light fs-6 d-none d-md-table-cell ${mediaAnual !== "-" && parseFloat(mediaAnual) < 5 ? "text-danger" : "text-primary"}">${mediaAnual}</td>
    <td class="d-none d-md-table-cell">${aprovadoStatus}</td>
</tr>`;
    });

    document.getElementById("boletimBody").innerHTML = tbody;

    let finalSit = "";
    if (mat) {
      if (dependencias === 0) {
        finalSit = '<span class="badge bg-success">Aprovado por Notas</span>';
      } else {
        finalSit = ` <span class="badge bg-danger">Em Recuperação (${dependencias} disc.)</span>`;
      }
    } else {
      finalSit = '<span class="badge bg-secondary">Sem turma</span>';
    }
    document.getElementById("lblSit").innerHTML = finalSit;

    document.getElementById("loadingIndicator").classList.add("d-none");
    document.getElementById("printArea").classList.remove("d-none");
  } catch (e) {
    console.error(e);
    alert("Erro ao processar as notas e matrículas do aluno.");
    document.getElementById("loadingIndicator").classList.add("d-none");
  }
}
function imprimirBoletim() {
    const conteudo = document.getElementById('boletimBody').innerHTML;
    const lblAno   = document.getElementById('lblAno').textContent;
    const lblNome  = document.getElementById('lblNome').textContent;
    const lblRa    = document.getElementById('lblRa').textContent;
    const lblResp  = document.getElementById('lblResp').textContent;
    const lblTurma = document.getElementById('lblTurma').textContent;
    const lblFreq  = document.getElementById('lblFreq').textContent;
    const lblSit   = document.getElementById('lblSit').innerHTML;
    const lblFreqTotal       = document.getElementById('lblFreqTotal').textContent;
    const lblFreqPresenca    = document.getElementById('lblFreqPresenca').textContent;
    const lblFreqFalta       = document.getElementById('lblFreqFalta').textContent;
    const lblFreqJustificada = document.getElementById('lblFreqJustificada').textContent;

    const html = `<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=1280, initial-scale=1">
    <title>Boletim Escolar - ${lblNome}</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
    <style>
        * { margin: 20; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Segoe UI', Arial, sans-serif;
            background: white;
            color: #2c3e50;
            padding: 20px;
            min-width: 1100px;
        }

        h2 { font-size: 20px; font-weight: 700; margin-bottom: 4px; }
        h4 { font-size: 15px; color: #555; margin-bottom: 16px; }

        .info-box {
            border: 1px solid #dee2e6;
            border-left: 5px solid #1a5276;
            border-radius: 6px;
            padding: 12px 16px;
            margin-bottom: 20px;
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 6px 24px;
            font-size: 13px;
        }
        .info-box strong { color: #555; }

        table {
            width: 100%;
            border-collapse: collapse;
            font-size: 11px;
            margin-bottom: 16px;
        }
        thead th {
            background: #f0f4f8;
            border: 1px solid #aaa;
            padding: 5px 4px;
            text-align: center;
            font-size: 10px;
            font-weight: 700;
            text-transform: uppercase;
            color: #555;
        }
        thead th.col-disc {
            text-align: left;
            min-width: 130px;
        }
        tbody td {
            border: 1px solid #ccc;
            padding: 4px 5px;
            text-align: center;
            vertical-align: middle;
        }
        tbody td:first-child {
            text-align: left;
            font-weight: 600;
            font-size: 11px;
        }
        .media-cell {
            background: #fcf4cd;
            font-weight: 700;
        }
        .media-final-cell {
            background: #f0f4f8;
            font-weight: 700;
            font-size: 13px;
        }
        .text-muted-note { color: #888; font-size: 10px; }

        .freq-box {
            border: 1px solid #dee2e6;
            border-radius: 6px;
            padding: 12px 16px;
            background: #f8f9fa;
        }
        .freq-box h6 { font-size: 13px; font-weight: 700; margin-bottom: 10px; }
        .freq-grid {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            text-align: center;
            gap: 8px;
        }
        .freq-grid .label { font-size: 11px; color: #888; margin-bottom: 2px; }
        .freq-grid .valor { font-size: 20px; font-weight: 700; }
        .freq-grid .valor.total   { color: #1a5276; }
        .freq-grid .valor.pres    { color: #27ae60; }
        .freq-grid .valor.falta   { color: #e74c3c; }
        .freq-grid .valor.just    { color: #f39c12; }

        @media print {
            @page { size: A4 landscape; margin: 0.5cm; }
            body {
                padding: 0;
                -webkit-print-color-adjust: exact;
                print-color-adjust: exact;
                min-width: 1100px;
            }
            .no-print { display: none !important; }
            thead th { background: #f0f4f8 !important; }
            .media-cell { background: #fcf4cd !important; }
            .media-final-cell { background: #f0f4f8 !important; }
            .freq-box { background: #f8f9fa !important; }
        }
    </style>
</head>
<body>

    <div class="text-center mb-3">
        <h2>Escola Horizonte do Saber</h2>
        <h4>Boletim Escolar — ${lblAno}</h4>
    </div>

    <div class="info-box">
        <div><strong>Aluno:</strong> ${lblNome}</div>
        <div><strong>RA:</strong> ${lblRa}</div>
        <div><strong>Responsável:</strong> ${lblResp}</div>
        <div><strong>Turma:</strong> ${lblTurma}</div>
        <div><strong>Presença Global:</strong> <b>${lblFreq}</b></div>
        <div><strong>Situação:</strong> ${lblSit}</div>
    </div>

    <table>
        <thead>
            <tr>
                <th rowspan="2" class="col-disc align-middle">Disciplina</th>
                <th colspan="3">1º Bimestre</th>
                <th colspan="3">2º Bimestre</th>
                <th colspan="3">3º Bimestre</th>
                <th colspan="3">4º Bimestre</th>
                <th rowspan="2" class="align-middle">Média<br>Final</th>
                <th rowspan="2" class="align-middle">Status</th>
            </tr>
            <tr>
                <th class="text-muted-note">AV1</th><th class="text-muted-note">AV2</th><th class="media-cell">Méd</th>
                <th class="text-muted-note">AV1</th><th class="text-muted-note">AV2</th><th class="media-cell">Méd</th>
                <th class="text-muted-note">AV1</th><th class="text-muted-note">AV2</th><th class="media-cell">Méd</th>
                <th class="text-muted-note">AV1</th><th class="text-muted-note">AV2</th><th class="media-cell">Méd</th>
            </tr>
        </thead>
        <tbody>
            ${conteudo}
        </tbody>
    </table>

    <div class="freq-box">
        <h6>Quadro de Frequências (Dias Letivos)</h6>
        <div class="freq-grid">
            <div>
                <div class="label">Total Ministrado</div>
                <div class="valor total">${lblFreqTotal}</div>
            </div>
            <div>
                <div class="label">Presenças</div>
                <div class="valor pres">${lblFreqPresenca}</div>
            </div>
            <div>
                <div class="label">Faltas</div>
                <div class="valor falta">${lblFreqFalta}</div>
            </div>
            <div>
                <div class="label">Justificadas</div>
                <div class="valor just">${lblFreqJustificada}</div>
            </div>
        </div>
    </div>

    <script>
        window.onload = () => {
            window.print();
            window.onafterprint = () => window.close();
        };
    <\/script>
</body>
</html>`;

    const iframe = document.createElement('iframe');
    iframe.style.cssText = 'position:fixed;top:-9999px;left:-9999px;width:1280px;height:900px;border:none;';
    document.body.appendChild(iframe);

    iframe.onload = () => {
        iframe.contentWindow.focus();
        // O próprio HTML do iframe chama window.print() no onload
        // Remove o iframe após a impressão
        iframe.contentWindow.onafterprint = () => {
            document.body.removeChild(iframe);
        };
    };

    const doc = iframe.contentDocument || iframe.contentWindow.document;
    doc.open();
    doc.write(html);
    doc.close();
}