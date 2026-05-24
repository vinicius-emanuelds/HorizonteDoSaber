//JS do professor
const API = "/api/professores";

document.addEventListener("DOMContentLoaded", () => {
  if (!localStorage.getItem("token")) {
    window.location.href = "/login";
    return;
  }
  buscarProfessores();
});

async function buscarProfessores() {
  const nome = document.getElementById("campoBusca").value;
  let url = `${API}?size=50&sort=nome`;
  if (nome) url += `&nome=${encodeURIComponent(nome)}`;
  const res = await apiFetch(url);
  const data = await res.json();
  renderizar(data.content || []);
}

function renderizar(list) {
  const tb = document.getElementById("listaProfessores");
  if (!list.length) {
    tb.innerHTML =
      '<tr><td colspan="7"><div class="empty-state"><p>Nenhum professor</p></div></td></tr>';
    return;
  }
  tb.innerHTML = list
    .map(
      (p) => `<tr>
        <td><strong>${p.codigoFuncional}</strong></td><td>${p.nome}</td><td>${p.cpf.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, "$1.$2.$3-$4")}</td><td>${p.email}</td>
        <td>${p.especialidadeDescricao || "-"}</td>
        <td><span class="badge-status ${p.ativo ? "ativo" : "inativo"}">${p.ativo ? "Ativo" : "Inativo"}</span></td>
        <td><button class="btn-action edit" title="Editar" onclick="editar(${p.id})">
        <i class="bi bi-pencil"></i></button>
            ${p.ativo
            ? `<button class="btn-action delete" title="Inativar" onclick="inativar(${p.id})">
                   <i class="bi bi-pause-circle"></i>
               </button>`
            : `<button class="btn-action view" title="Ativar" onclick="ativar(${p.id})">
                   <i class="bi bi-play-circle"></i>
               </button>`
        }</tr>`
    )
    .join("");
}

function abrirModal(p) {
  document.getElementById("modalTitle").textContent = p
    ? "Editar Professor"
    : "Novo Professor";
  document.getElementById("profId").value = p?.id || "";
  document.getElementById("profNome").value = p?.nome || "";
  document.getElementById("profDataNasc").value = p?.dataNascimento || "";
  document.getElementById("profCpf").value = p?.cpf || "";
  document.getElementById("profEmail").value = p?.email || "";
  document.getElementById("profEspecialidade").value =
    p?.especialidade || "REGENTE";
  new bootstrap.Modal(document.getElementById("modalProfessor")).show();
}

async function editar(id) {
  const r = await apiFetch(`${API}/${id}`);
  abrirModal(await r.json());
}

async function salvar() {
  const id = document.getElementById("profId").value;
  const body = {
    nome: document.getElementById("profNome").value,
    dataNascimento: document.getElementById("profDataNasc").value,
    cpf: document.getElementById("profCpf").value,
    email: document.getElementById("profEmail").value,
    especialidade: document.getElementById("profEspecialidade").value,
  };
  const res = await apiFetch(id ? `${API}/${id}` : API, {
    method: id ? "PUT" : "POST",
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    const e = await res.json();
    alert(e.message || "Erro");
    return;
  }
  bootstrap.Modal.getInstance(document.getElementById("modalProfessor")).hide();
  buscarProfessores();
}

async function inativar(id) {
  if (!confirm("Inativar?")) return;
  await apiFetch(`${API}/${id}/inativar`, { method: "PATCH" });
  buscarProfessores();
}

async function ativar(id) {
  if (!confirm("Ativar?")) return;
  await apiFetch(`${API}/${id}/ativar`, { method: "PATCH" });
  buscarProfessores();
}
