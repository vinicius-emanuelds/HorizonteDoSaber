/**
 * auth.js — Interceptor global de autenticação JWT + utilitários de UI.
 */

// ─── Toast notifications ──────────────────────────────────────────────────────

/**
 * Exibe um toast flutuante no canto inferior direito.
 * @param {string} msg       Mensagem a exibir
 * @param {'success'|'error'|'warning'|'info'} tipo  Tipo visual
 * @param {number}  duracao  Duração em ms (padrão 4000)
 */
function showToast(msg, tipo = 'info', duracao = 4000) {
    let container = document.getElementById('toastContainer');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toastContainer';
        container.style.cssText = [
            'position:fixed', 'bottom:24px', 'right:24px', 'z-index:9999',
            'display:flex', 'flex-direction:column-reverse', 'gap:8px',
            'pointer-events:none'
        ].join(';');
        document.body.appendChild(container);
    }

    const icons = { success: 'check-circle-fill', error: 'x-circle-fill', warning: 'exclamation-triangle-fill', info: 'info-circle-fill' };
    const colors = { success: '#27ae60', error: '#e74c3c', warning: '#f39c12', info: '#3498db' };

    const toast = document.createElement('div');
    toast.style.cssText = [
        'background:#fff', 'border-radius:12px', 'padding:14px 18px',
        'box-shadow:0 8px 30px rgba(0,0,0,0.15)', 'display:flex', 'align-items:center',
        'gap:12px', 'font-family:Inter,sans-serif', 'font-size:14px', 'font-weight:500',
        'color:#2c3e50', 'max-width:360px', 'pointer-events:auto',
        `border-left:4px solid ${colors[tipo]}`,
        'transform:translateX(120%)', 'transition:transform 0.35s cubic-bezier(0.4,0,0.2,1)',
        'will-change:transform'
    ].join(';');

    toast.innerHTML = `
        <i class="bi bi-${icons[tipo]}" style="font-size:20px;color:${colors[tipo]};flex-shrink:0"></i>
        <span style="flex:1;line-height:1.4">${msg}</span>
        <button onclick="this.closest('div').remove()" style="background:none;border:none;cursor:pointer;color:#7f8c8d;font-size:16px;padding:0;flex-shrink:0;line-height:1">✕</button>
    `;

    container.appendChild(toast);
    requestAnimationFrame(() => {
        requestAnimationFrame(() => { toast.style.transform = 'translateX(0)'; });
    });

    setTimeout(() => {
        toast.style.transform = 'translateX(120%)';
        setTimeout(() => toast.remove(), 350);
    }, duracao);
}

// ─── Gerenciamento de cookie ──────────────────────────────────────────────────

/**
 * Salva o JWT em cookie com a validade configurada.
 */
function setCookieJwt(token, maxAgeSeconds = 1800) {
    const expira = new Date();
    expira.setTime(expira.getTime() + (maxAgeSeconds * 1000));
    document.cookie = [
        'jwt=' + token,
        'expires=' + expira.toUTCString(),
        'path=/',
        'SameSite=Strict'
    ].join('; ');
}

// ─── Headers para chamadas fetch ─────────────────────────────────────────────

function authHeaders(extra = {}) {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': 'Bearer ' + token } : {}),
        ...extra
    };
}

// ─── Wrapper fetch com interceptação 401/403 ─────────────────────────────────

async function apiFetch(url, opts = {}) {
    const options = {
        ...opts,
        headers: {
            ...authHeaders(),
            ...(opts.headers || {})
        }
    };

    let response;
    try {
        response = await fetch(url, options);
    } catch (err) {
        throw err;
    }

    if (response.status === 401 || response.status === 403) {
        logout(true);
        throw new Error('Sessão expirada ou sem permissão. Redirecionando para login...');
    }

    return response;
}

// ─── Logout ──────────────────────────────────────────────────────────────────

function logout(expired = false) {
    localStorage.removeItem('token');
    localStorage.removeItem('usuario');
    localStorage.removeItem('role');
    localStorage.removeItem('login');
    localStorage.removeItem('nome');
    localStorage.removeItem('primeiroAcesso');
    localStorage.removeItem('senhaExpirada');

    // Remove o cookie jwt para que o backend não reconheça mais a sessão
    document.cookie = 'jwt=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; SameSite=Strict';

    window.location.href = '/login' + (expired ? '?expired=1' : '');
}

// ─── Verificação de acesso obrigatório à troca de senha ───────────────────────

/**
 * Chame este método no início de cada página protegida (exceto /trocar-senha).
 * Se o usuário ainda tiver primeiroAcesso=true ou senha expirada, redireciona.
 */
function verificarTrocaSenhaObrigatoria() {
    const primeiroAcesso = localStorage.getItem('primeiroAcesso') === 'true';
    const senhaExpirada  = localStorage.getItem('senhaExpirada')  === 'true';
    if (primeiroAcesso || senhaExpirada) {
        const motivo = senhaExpirada ? 'expirada' : 'obrigatorio';
        window.location.replace('/trocar-senha?' + motivo + '=1');
    }
}