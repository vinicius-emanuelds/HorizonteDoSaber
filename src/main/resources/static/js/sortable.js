// sortable.js
document.addEventListener('DOMContentLoaded', () => {
    // Adiciona evento de clique em todos os cabeçalhos de tabela
    const initSortable = () => {
        document.querySelectorAll('table thead th').forEach(th => {
            const text = th.innerText.trim().toLowerCase();
            // Ignora colunas vazias, de ações ou checkbox
            if (!text || text === 'ações' || text === 'status' || th.querySelector('input')) return;
            
            // Verifica se já foi inicializado
            if (th.hasAttribute('data-sortable')) return;
            th.setAttribute('data-sortable', 'true');
            
            th.style.cursor = 'pointer';
            th.title = 'Clique para ordenar';
            th.innerHTML += ' <i class="bi bi-arrow-down-up small text-muted ms-1" style="font-size: 0.75rem;"></i>';
            
            th.addEventListener('click', () => {
                const table = th.closest('table');
                const tbody = table.querySelector('tbody');
                if (!tbody) return;
                
                const trs = Array.from(tbody.querySelectorAll('tr'));
                // Ignora se a tabela estiver vazia ou com colspan (ex: loading spinner)
                if (trs.length === 0 || (trs[0].cells.length === 1 && trs[0].cells[0].colSpan > 1)) return;
                
                const index = Array.from(th.parentNode.children).indexOf(th);
                const isAsc = th.classList.contains('asc');
                
                // Limpa classes de outros headers
                table.querySelectorAll('th').forEach(h => {
                    h.classList.remove('asc', 'desc');
                    const icon = h.querySelector('i.bi');
                    if (icon) icon.className = 'bi bi-arrow-down-up small text-muted ms-1';
                });
                
                // Adiciona classe e ícone no atual
                th.classList.add(isAsc ? 'desc' : 'asc');
                const currentIcon = th.querySelector('i.bi');
                if (currentIcon) {
                    currentIcon.className = isAsc ? 'bi bi-arrow-down small text-primary ms-1' : 'bi bi-arrow-up small text-primary ms-1';
                }
                
                trs.sort((a, b) => {
                    const aText = a.cells[index]?.innerText.trim() || '';
                    const bText = b.cells[index]?.innerText.trim() || '';
                    
                    // Tenta ordenação por data (dd/mm/yyyy)
                    const dateRegex = /^(\d{2})\/(\d{2})\/(\d{4})$/;
                    if (dateRegex.test(aText) && dateRegex.test(bText)) {
                        const [, d1, m1, y1] = aText.match(dateRegex);
                        const [, d2, m2, y2] = bText.match(dateRegex);
                        const dateA = new Date(y1, m1 - 1, d1).getTime();
                        const dateB = new Date(y2, m2 - 1, d2).getTime();
                        return isAsc ? dateB - dateA : dateA - dateB;
                    }
                    
                    // Tenta ordenação numérica
                    const aNum = parseFloat(aText.replace(',', '.'));
                    const bNum = parseFloat(bText.replace(',', '.'));
                    
                    if (!isNaN(aNum) && !isNaN(bNum)) {
                        return isAsc ? bNum - aNum : aNum - bNum;
                    }
                    
                    // Ordenação alfabética padrão
                    return isAsc ? bText.localeCompare(aText) : aText.localeCompare(bText);
                });
                
                trs.forEach(tr => tbody.appendChild(tr));
            });
        });
    };

    initSortable();

    // Como as tabelas podem ser carregadas via fetch dinamicamente (ex: listaAnos), 
    // observamos mudanças no DOM para aplicar a ordenação nos novos headers se necessário,
    // ou apenas chamamos initSortable após renderizar a tabela.
    const observer = new MutationObserver((mutations) => {
        for (const m of mutations) {
            if (m.addedNodes.length > 0) {
                initSortable();
            }
        }
    });
    observer.observe(document.body, { childList: true, subtree: true });
});
