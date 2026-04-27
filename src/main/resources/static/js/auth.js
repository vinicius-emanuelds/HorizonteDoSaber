/**
 * auth.js — Interceptor global de autenticação JWT.
 */

// ─── Gerenciamento de cookie ──────────────────────────────────────────────────

/**
 * Salva o JWT em cookie com validade de 1 dia.
 * O browser envia este cookie automaticamente em toda navegação de página,
 * permitindo que o JwtAuthFilter autentique requisições SSR.
 */
function setCookieJwt(token) {
    const expira = new Date();
    expira.setTime(expira.getTime() + (24 * 60 * 60 * 1000));
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

    // Remove o cookie jwt para que o backend não reconheça mais a sessão
    document.cookie = 'jwt=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; SameSite=Strict';

    window.location.href = '/login' + (expired ? '?expired=1' : '');
}