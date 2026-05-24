// JS da tela de Usuários
const API = '/api/usuarios';
const SENHA_PADRAO = 'Siga2025@';

document.addEventListener('DOMContentLoaded', () => {
    if (!localStorage.getItem('token')) { window.location.href = '/login'; return; }
    buscar();
});

async function buscar() {
    const nome = document.getElementById('campoBusca').value;
    let url = `${API}?size=50&sort=nomeCompleto`;
    if (nome) url += `&nome=${encodeURIComponent(nome)}`;
    const data = await (await apiFetch(url)).json();
    renderizar(data.content || []);
}

const roleLabels = { ADMIN: 'Administrador', COORDENADOR: 'Coordenador', OPERADOR: 'Operador', PROFESSOR: 'Professor' };

function renderizar(list) {
    const tb = document.getElementById('lista');
    if (!list.length) {
        tb.innerHTML = '<tr><td colspan="6"><div class="empty-state"><p>Nenhum usuário</p></div></td></tr>';
        return;
    }
    tb.innerHTML = list.map(u => `<tr>
        <td><strong>${u.codigo}</strong></td><td>${u.nomeCompleto}</td><td>${u.login}</td>
        <td>${roleLabels[u.role] || u.role}</td>
        <td><span class="badge-status ${u.ativo ? (u.bloqueado ? 'trancada' : 'ativo') : 'inativo'}">${u.bloqueado ? 'Bloqueado' : (u.ativo ? 'Ativo' : 'Inativo')}</span></td>
        <td>
            <button class="btn-action edit" onclick="editar(${u.id})"><i class="bi bi-pencil"></i></button>
            ${u.bloqueado ? `<button class="btn-action view" onclick="desbloquear(${u.id})" title="Desbloquear"><i class="bi bi-unlock"></i></button>` : ''}
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

    // Na edição, campo senha é opcional; na criação é obrigatório
    const senhaInput = document.getElementById('userSenha');
    if (u) {
        senhaInput.placeholder = 'Deixe em branco para manter a atual';
        senhaInput.removeAttribute('required');
    } else {
        senhaInput.placeholder = 'Mínimo 8 caracteres';
        senhaInput.setAttribute('required', '');
    }

    document.getElementById('userRole').value = u?.role || 'OPERADOR';

    // Limpa mensagem de validação anterior
    const senhaHelp = document.getElementById('senhaHelp');
    if (senhaHelp) { senhaHelp.textContent = ''; senhaHelp.style.display = 'none'; }

    new bootstrap.Modal(document.getElementById('modalUsuario')).show();
}

async function editar(id) {
    abrirModal(await (await apiFetch(`${API}/${id}`)).json());
}

// ─── Validação de senha em tempo real no modal ─────────────────────────────

document.addEventListener('input', (e) => {
    if (e.target.id !== 'userSenha') return;
    const senha = e.target.value;
    const help = document.getElementById('senhaHelp');
    if (!help) return;

    if (!senha) { help.style.display = 'none'; return; }

    const erros = validarSenhaTexto(senha);
    if (erros.length === 0) {
        help.style.display = 'block';
        help.style.color = 'var(--success)';
        help.innerHTML = '<i class="bi bi-check-circle-fill me-1"></i>Senha válida';
    } else {
        help.style.display = 'block';
        help.style.color = 'var(--danger)';
        help.innerHTML = '<i class="bi bi-exclamation-circle-fill me-1"></i>' + erros.join(' · ');
    }
});

/**
 * Valida a senha e retorna lista de erros (vazia = válida).
 */
function validarSenhaTexto(senha) {
    const erros = [];
    if (senha.length < 8)            erros.push('Mínimo 8 caracteres');
    if (!/[A-Z]/.test(senha))        erros.push('Uma letra maiúscula');
    if (!/[a-z]/.test(senha))        erros.push('Uma letra minúscula');
    if (!/\d/.test(senha))           erros.push('Um número');
    if (senha === SENHA_PADRAO)       erros.push('Não pode ser a senha padrão do sistema');
    return erros;
}

// ─── Salvar usuário (criar ou atualizar) ──────────────────────────────────

async function salvar() {
    const id    = document.getElementById('userId').value;
    const senha = document.getElementById('userSenha').value;

    // Validação de senha quando preenchida
    if (senha) {
        const erros = validarSenhaTexto(senha);
        if (erros.length > 0) {
            showToast('Senha inválida: ' + erros.join(', ') + '.', 'error', 6000);
            return;
        }
    } else if (!id) {
        // Criação sem senha
        showToast('A senha é obrigatória para criar um novo usuário.', 'warning');
        return;
    }

    const body = {
        nomeCompleto: document.getElementById('userNome').value,
        email:        document.getElementById('userEmail').value,
        login:        document.getElementById('userLogin').value,
        role:         document.getElementById('userRole').value,
        ...(senha ? { senha } : {})   // só envia senha se preenchida
    };

    try {
        const res = await apiFetch(id ? `${API}/${id}` : API, {
            method: id ? 'PUT' : 'POST',
            body: JSON.stringify(body)
        });

        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            // Extrai mensagens de validação do Bean Validation (campo a campo)
            if (err.errors) {
                const msgs = Object.values(err.errors).join('\n');
                showToast(msgs, 'error', 6000);
            } else {
                const msg = err.novaSenha || err.senha || err.message || 'Erro ao salvar usuário.';
                showToast(msg, 'error', 6000);
            }
            return;
        }

        bootstrap.Modal.getInstance(document.getElementById('modalUsuario')).hide();
        showToast(id ? 'Usuário atualizado com sucesso!' : 'Usuário criado com sucesso!', 'success');
        buscar();

    } catch (err) {
        showToast('Erro de conexão. Tente novamente.', 'error');
    }
}

async function inativar(id) {
    if (!confirm('Deseja inativar este usuário?')) return;
    await apiFetch(`${API}/${id}/inativar`, { method: 'PATCH' });
    showToast('Usuário inativado.', 'info');
    buscar();
}

async function desbloquear(id) {
    await apiFetch(`${API}/${id}/desbloquear`, { method: 'PATCH' });
    showToast('Usuário desbloqueado com sucesso!', 'success');
    buscar();
}
