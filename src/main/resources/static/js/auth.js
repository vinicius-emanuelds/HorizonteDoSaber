/**
 * auth.js — Interceptor global de autenticação JWT.
 *
 * Substitui o `fetch` nativo por `apiFetch`, que:
 *  - Injeta o cabeçalho Authorization automaticamente
 *  - Intercepta respostas 401 (token expirado/inválido) e 403 (sem permissão)
 *    e redireciona para /login, limpando os dados de sessão do localStorage.
 *
 * Como usar nos demais JS:
 *   const res = await apiFetch('/api/alunos', { method: 'GET' });
 */

/**
 * Retorna os cabeçalhos padrão com o token JWT do localStorage.
 */
function authHeaders(extra = {}) {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': 'Bearer ' + token } : {}),
        ...extra
    };
}

/**
 * Wrapper do fetch com interceptação de 401/403.
 * Aceita os mesmos parâmetros do fetch nativo.
 */
async function apiFetch(url, opts = {}) {
    // Injeta headers de autenticação caso não sejam sobrescritos
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
        // Erro de rede (offline, CORS bloqueado etc.) — propaga normalmente
        throw err;
    }

    if (response.status === 401 || response.status === 403) {
        logout(true);
        // Interrompe a cadeia de promessas do chamador
        throw new Error('Sessão expirada ou sem permissão. Redirecionando para login...');
    }

    return response;
}

/**
 * Encerra a sessão do usuário: limpa localStorage e redireciona para /login.
 * @param {boolean} expired - se true, passa query param ?expired=1 para exibir aviso na tela de login
 */
function logout(expired = false) {
    localStorage.removeItem('token');
    localStorage.removeItem('usuario');
    localStorage.removeItem('role');
    window.location.href = '/login' + (expired ? '?expired=1' : '');
}
