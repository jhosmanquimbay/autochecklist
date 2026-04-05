(function () {
    const ROTATION_INTERVAL = 6200;
    const REFRESH_INTERVAL = 45000;
    const state = {
        items: [],
        index: 0,
        rotationTimer: null,
        refreshTimer: null,
        signature: ''
    };

    const fallbackItems = [
        {
            placement: 'client',
            eyebrow: 'Portal cliente',
            title: 'Promociones visibles tambien en tu cuenta',
            description: 'Un carrusel compacto para perfil, compras y favoritos con imagen, boton y mensaje breve.',
            ctaText: 'Ir a compras',
            ctaUrl: '/cliente/compras',
            metricValue: 'VIP',
            metricLabel: 'beneficio',
            theme: 'blue',
            icon: 'fas fa-user-shield',
            imageUrl: null,
            displayOrder: 1
        },
        {
            placement: 'client',
            eyebrow: 'Seguimiento',
            title: 'Fideliza con campanas dentro del portal',
            description: 'Muestra bonos, recordatorios o beneficios para clientes autenticados sin tocar codigo.',
            ctaText: 'Ver favoritos',
            ctaUrl: '/cliente/favoritos',
            metricValue: '3x',
            metricLabel: 'impacto',
            theme: 'emerald',
            icon: 'fas fa-gem',
            imageUrl: null,
            displayOrder: 2
        }
    ];

    function escapeHtml(value) {
        return String(value || '')
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function normalizePlacement(value) {
        if (!value) {
            return 'hero';
        }

        const normalized = String(value).trim().toLowerCase();
        if (normalized === 'client') {
            return 'client';
        }
        if (normalized === 'showcase') {
            return 'catalog';
        }
        if (normalized === 'catalog') {
            return 'catalog';
        }
        return 'hero';
    }

    function normalizeItem(item, index) {
        return {
            placement: normalizePlacement(item && item.placement),
            eyebrow: item && item.eyebrow ? item.eyebrow : 'Campana activa',
            title: item && item.title ? item.title : 'Promocion para clientes',
            description: item && item.description ? item.description : 'Campanas editables directamente desde administracion.',
            ctaText: item && item.ctaText ? item.ctaText : 'Ver detalle',
            ctaUrl: item && item.ctaUrl ? item.ctaUrl : '/cliente/perfil',
            metricValue: item && item.metricValue ? item.metricValue : 'VIP',
            metricLabel: item && item.metricLabel ? item.metricLabel : 'beneficio',
            theme: item && item.theme ? item.theme : ['blue', 'emerald', 'gold'][index % 3],
            icon: item && item.icon ? item.icon : 'fas fa-bullhorn',
            imageUrl: item && item.imageUrl ? item.imageUrl : null,
            displayOrder: item && item.displayOrder ? Number(item.displayOrder) : index + 1
        };
    }

    function compareItems(left, right) {
        const orderDelta = (left.displayOrder || 0) - (right.displayOrder || 0);
        if (orderDelta !== 0) {
            return orderDelta;
        }
        return String(left.title || '').localeCompare(String(right.title || ''));
    }

    function buildSignature(items) {
        return JSON.stringify(items.map(function (item) {
            return [item.title, item.description, item.ctaUrl, item.imageUrl, item.displayOrder];
        }));
    }

    function getSection() {
        return document.querySelector('.client-campaigns');
    }

    function getSource() {
        const section = getSection();
        return section && section.dataset.bannerSource ? section.dataset.bannerSource : '/home-banners.json';
    }

    function renderIndicators() {
        const container = document.getElementById('clientCampaignIndicators');
        if (!container) {
            return;
        }

        container.innerHTML = state.items.map(function (item, index) {
            const activeClass = index === state.index ? ' active' : '';
            return '<button type="button" class="client-campaign-indicator' + activeClass + '" data-client-indicator="' + index + '" aria-label="Ver campana ' + escapeHtml(item.title) + '"></button>';
        }).join('');
    }

    function render() {
        const section = getSection();
        const viewport = document.getElementById('clientCampaignViewport');
        if (!section || !viewport) {
            return;
        }

        if (!state.items.length) {
            section.hidden = true;
            return;
        }

        const item = state.items[state.index];
        const mediaMarkup = item.imageUrl
            ? '<img src="' + escapeHtml(item.imageUrl) + '" alt="' + escapeHtml(item.title) + '" class="client-campaign-image">'
            : '<div class="client-campaign-icon"><i class="' + escapeHtml(item.icon) + '"></i></div>';

        viewport.innerHTML = '' +
            '<article class="client-campaign-slide theme-' + escapeHtml(item.theme) + (item.imageUrl ? ' has-image' : '') + '">' +
                '<div class="client-campaign-media">' + mediaMarkup + '</div>' +
                '<div class="client-campaign-shade"></div>' +
                '<div class="client-campaign-copy">' +
                    '<span class="client-campaign-chip">' + escapeHtml(item.eyebrow) + '</span>' +
                    '<h2>' + escapeHtml(item.title) + '</h2>' +
                    '<p>' + escapeHtml(item.description) + '</p>' +
                    '<div class="client-campaign-actions">' +
                        '<a href="' + escapeHtml(item.ctaUrl) + '" class="btn-client-primary">' + escapeHtml(item.ctaText) + ' <i class="fas fa-arrow-right"></i></a>' +
                        '<div class="client-campaign-metric"><strong>' + escapeHtml(item.metricValue) + '</strong><span>' + escapeHtml(item.metricLabel) + '</span></div>' +
                    '</div>' +
                '</div>' +
            '</article>';

        section.hidden = false;
        renderIndicators();
    }

    function stopRotation() {
        if (state.rotationTimer) {
            window.clearInterval(state.rotationTimer);
            state.rotationTimer = null;
        }
    }

    function startRotation() {
        stopRotation();
        if (state.items.length < 2) {
            return;
        }
        state.rotationTimer = window.setInterval(function () {
            move(1, false);
        }, ROTATION_INTERVAL);
    }

    function move(step, restartTimer) {
        if (!state.items.length) {
            return;
        }

        state.index = (state.index + step + state.items.length) % state.items.length;
        render();
        if (restartTimer !== false) {
            startRotation();
        }
    }

    async function loadItems() {
        try {
            const response = await window.fetch(getSource(), { headers: { Accept: 'application/json' } });
            if (!response.ok) {
                throw new Error('No se pudo cargar');
            }

            const payload = await response.json();
            const normalized = Array.isArray(payload)
                ? payload.map(normalizeItem).sort(compareItems)
                : [];
            const scoped = normalized.filter(function (item) {
                return item.placement === 'client';
            });
            return scoped.length ? scoped : fallbackItems.map(normalizeItem).sort(compareItems);
        } catch (error) {
            return fallbackItems.map(normalizeItem).sort(compareItems);
        }
    }

    async function refresh() {
        const nextItems = await loadItems();
        const nextSignature = buildSignature(nextItems);
        if (nextSignature === state.signature) {
            return;
        }

        state.items = nextItems;
        state.signature = nextSignature;
        state.index = Math.min(state.index, Math.max(state.items.length - 1, 0));
        render();
        startRotation();
    }

    function bindEvents() {
        document.addEventListener('click', function (event) {
            const arrow = event.target.closest('[data-client-campaign]');
            if (arrow) {
                move(arrow.dataset.clientCampaign === 'prev' ? -1 : 1, true);
                return;
            }

            const indicator = event.target.closest('[data-client-indicator]');
            if (indicator) {
                const nextIndex = Number(indicator.dataset.clientIndicator);
                if (!Number.isNaN(nextIndex)) {
                    state.index = nextIndex;
                    render();
                    startRotation();
                }
            }
        });
    }

    function init() {
        if (!getSection()) {
            return;
        }

        bindEvents();
        refresh();
        state.refreshTimer = window.setInterval(refresh, REFRESH_INTERVAL);
        window.addEventListener('beforeunload', function () {
            stopRotation();
            if (state.refreshTimer) {
                window.clearInterval(state.refreshTimer);
                state.refreshTimer = null;
            }
        }, { once: true });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init, { once: true });
    } else {
        init();
    }
})();