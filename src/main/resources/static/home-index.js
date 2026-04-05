(function () {
    const PLACEMENTS = {
        hero: 'hero',
        catalog: 'catalog',
        client: 'client'
    };

    const ROTATION_INTERVAL = 5600;
    const REFRESH_INTERVAL = 45000;
    const state = {
        heroItems: [],
        catalogItems: [],
        heroIndex: 0,
        heroTimer: null,
        refreshTimer: null,
        heroSignature: '',
        catalogSignature: ''
    };

    const fallbackBanners = [
        {
            placement: PLACEMENTS.hero,
            eyebrow: 'Lanzamiento activo',
            title: 'SUVs con entrega inmediata',
            description: 'Campanas visuales de alto impacto para abrir el recorrido con una imagen protagonista y un llamado claro.',
            ctaText: 'Explorar inventario',
            ctaUrl: '#vehiculos',
            metricValue: '48h',
            metricLabel: 'respuesta',
            theme: 'gold',
            icon: 'fas fa-car-side',
            imageUrl: null,
            displayOrder: 1
        },
        {
            placement: PLACEMENTS.hero,
            eyebrow: 'Financiacion',
            title: 'Ofertas que se ven y se entienden rapido',
            description: 'Usa este espacio para mostrar la foto de una promocion fuerte, sin bloques editoriales innecesarios.',
            ctaText: 'Ver promociones',
            ctaUrl: '#catalogPromoBand',
            metricValue: '0%',
            metricLabel: 'entrada',
            theme: 'blue',
            icon: 'fas fa-percent',
            imageUrl: null,
            displayOrder: 2
        },
        {
            placement: PLACEMENTS.catalog,
            eyebrow: 'Promocion home',
            title: 'Agenda una prueba de manejo',
            description: 'Tarjeta secundaria para empujar acciones concretas sin quitar protagonismo al catalogo.',
            ctaText: 'Reservar ahora',
            ctaUrl: '#contacto',
            metricValue: '15m',
            metricLabel: 'confirmacion',
            theme: 'gold',
            icon: 'fas fa-calendar-check',
            imageUrl: null,
            displayOrder: 1
        },
        {
            placement: PLACEMENTS.catalog,
            eyebrow: 'Beneficio vigente',
            title: 'Modelos con bono especial',
            description: 'Perfecto para empujar una categoria, un bono o una promocion puntual dentro del inicio.',
            ctaText: 'Ver destacados',
            ctaUrl: '#destacados',
            metricValue: 'Top',
            metricLabel: 'consulta',
            theme: 'emerald',
            icon: 'fas fa-award',
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

    function hasText(value) {
        return typeof value === 'string' ? value.trim().length > 0 : Boolean(value);
    }

    function optionalText(value) {
        return hasText(value) ? String(value).trim() : null;
    }

    function normalizePlacement(value) {
        if (!value) {
            return PLACEMENTS.hero;
        }

        const normalized = String(value).trim().toLowerCase();
        if (normalized === 'showcase') {
            return PLACEMENTS.catalog;
        }

        if (normalized === PLACEMENTS.catalog || normalized === PLACEMENTS.client) {
            return normalized;
        }

        return PLACEMENTS.hero;
    }

    function defaultDescriptionForPlacement(placement) {
        if (placement === PLACEMENTS.catalog) {
            return 'Promocion breve para apoyar el recorrido del inicio y dirigir el siguiente clic.';
        }

        if (placement === PLACEMENTS.client) {
            return 'Promocion visible dentro del portal cliente para destacar beneficios y acciones activas.';
        }

        return 'Promocion principal con imagen protagonista, mensaje corto y llamada a la accion directa.';
    }

    function rankByPlacement(placement) {
        if (placement === PLACEMENTS.catalog) {
            return 1;
        }
        if (placement === PLACEMENTS.client) {
            return 2;
        }
        return 0;
    }

    function normalizeBanner(item, index) {
        const themes = ['gold', 'blue', 'emerald'];
        const placement = normalizePlacement(item && item.placement);
        return {
            placement: placement,
            eyebrow: optionalText(item && item.eyebrow),
            title: optionalText(item && item.title),
            description: optionalText(item && item.description),
            ctaText: optionalText(item && item.ctaText),
            ctaUrl: optionalText(item && item.ctaUrl),
            metricValue: optionalText(item && item.metricValue),
            metricLabel: optionalText(item && item.metricLabel),
            theme: item && item.theme ? item.theme : themes[index % themes.length],
            icon: item && item.icon ? item.icon : 'fas fa-bullhorn',
            imageUrl: item && item.imageUrl ? item.imageUrl : null,
            displayOrder: item && item.displayOrder ? Number(item.displayOrder) : index + 1
        };
    }

    function compareBanners(left, right) {
        const placementDelta = rankByPlacement(left.placement) - rankByPlacement(right.placement);
        if (placementDelta !== 0) {
            return placementDelta;
        }

        const orderDelta = (left.displayOrder || 0) - (right.displayOrder || 0);
        if (orderDelta !== 0) {
            return orderDelta;
        }

        return String(left.title || '').localeCompare(String(right.title || ''));
    }

    function buildSignature(items) {
        return JSON.stringify(items.map(function (item) {
            return [
                item.placement,
                item.title,
                item.description,
                item.ctaText,
                item.ctaUrl,
                item.metricValue,
                item.metricLabel,
                item.theme,
                item.imageUrl,
                item.displayOrder
            ];
        }));
    }

    function getDataSource() {
        const shell = document.querySelector('.campaign-billboard-shell');
        return shell && shell.dataset.bannerSource ? shell.dataset.bannerSource : '/home-banners.json';
    }

    function withFallback(items) {
        const normalized = Array.isArray(items)
            ? items.map(normalizeBanner).sort(compareBanners)
            : [];

        if (normalized.length) {
            return normalized;
        }

        return fallbackBanners.map(normalizeBanner).sort(compareBanners);
    }

    function getPlacementItems(items, placement) {
        return items.filter(function (item) {
            return item.placement === placement;
        });
    }

    function renderHeroIndicators() {
        const container = document.getElementById('campaignBillboardIndicators');
        if (!container) {
            return;
        }

        container.innerHTML = state.heroItems.map(function (item, index) {
            const activeClass = index === state.heroIndex ? ' active' : '';
            return '<button type="button" class="campaign-indicator' + activeClass + '" data-home-indicator="' + index + '" aria-label="Ver campana ' + escapeHtml(item.title) + '"></button>';
        }).join('');
    }

    function renderHeroSlide() {
        const section = document.getElementById('promociones');
        const viewport = document.getElementById('campaignBillboardViewport');
        if (!section || !viewport) {
            return;
        }

        if (!state.heroItems.length) {
            section.hidden = true;
            return;
        }

        const item = state.heroItems[state.heroIndex];
        const hasEyebrow = hasText(item.eyebrow);
        const hasTitle = hasText(item.title);
        const hasDescription = hasText(item.description);
        const hasCta = hasText(item.ctaText) && hasText(item.ctaUrl);
        const hasMetric = hasText(item.metricValue) || hasText(item.metricLabel);
        const hasActions = hasCta || hasMetric;
        const hasCopy = hasEyebrow || hasTitle || hasDescription || hasActions;
        const imageMarkup = item.imageUrl
            ? '<img src="' + escapeHtml(item.imageUrl) + '" alt="' + escapeHtml(item.title) + '" class="campaign-slide-image">'
            : '<div class="campaign-slide-fallback-icon"><i class="' + escapeHtml(item.icon) + '"></i></div>';

        const metricMarkup = hasMetric
            ? '<div class="campaign-slide-metric">' +
                (hasText(item.metricValue) ? '<strong>' + escapeHtml(item.metricValue) + '</strong>' : '') +
                (hasText(item.metricLabel) ? '<span>' + escapeHtml(item.metricLabel) + '</span>' : '') +
              '</div>'
            : '';

        const ctaMarkup = hasCta
            ? '<a href="' + escapeHtml(item.ctaUrl) + '" class="campaign-slide-button">' + escapeHtml(item.ctaText) + ' <i class="fas fa-arrow-right"></i></a>'
            : '';

        const copyMarkup = hasCopy
            ? '<div class="campaign-slide-copy">' +
                (hasEyebrow ? '<span class="campaign-slide-chip">' + escapeHtml(item.eyebrow) + '</span>' : '') +
                (hasTitle ? '<h3>' + escapeHtml(item.title) + '</h3>' : '') +
                (hasDescription ? '<p>' + escapeHtml(item.description) + '</p>' : '') +
                (hasActions ? '<div class="campaign-slide-actions">' + ctaMarkup + metricMarkup + '</div>' : '') +
              '</div>'
            : '';

        viewport.innerHTML = '' +
            '<article class="campaign-slide theme-' + escapeHtml(item.theme) + (item.imageUrl ? ' has-image' : '') + (hasCopy ? '' : ' image-only') + '">' +
                '<div class="campaign-slide-media">' + imageMarkup + '</div>' +
                '<div class="campaign-slide-shade"></div>' +
                copyMarkup +
            '</article>';

        section.hidden = false;
        renderHeroIndicators();
    }

    function renderCatalogCards() {
        const band = document.getElementById('catalogPromoBand');
        const grid = document.getElementById('catalogPromoGrid');
        if (!band || !grid) {
            return;
        }

        if (!state.catalogItems.length) {
            band.hidden = true;
            return;
        }

        grid.innerHTML = state.catalogItems.slice(0, 3).map(function (item) {
            const hasEyebrow = hasText(item.eyebrow);
            const hasTitle = hasText(item.title);
            const hasDescription = hasText(item.description);
            const hasMetric = hasText(item.metricValue) || hasText(item.metricLabel);
            const hasCta = hasText(item.ctaText) && hasText(item.ctaUrl);
            const mediaMarkup = item.imageUrl
                ? '<div class="catalog-promo-media"><img src="' + escapeHtml(item.imageUrl) + '" alt="' + escapeHtml(item.title) + '"></div>'
                : '<div class="catalog-promo-media"><div class="catalog-promo-icon"><i class="' + escapeHtml(item.icon) + '"></i></div></div>';

            const topMeta = (hasEyebrow || hasMetric)
                ? '<div class="catalog-promo-top">' +
                    (hasEyebrow ? '<span class="catalog-promo-kicker">' + escapeHtml(item.eyebrow) + '</span>' : '') +
                    (hasMetric ? '<span class="catalog-promo-metric">' + escapeHtml((item.metricValue || '').trim() + (item.metricValue && item.metricLabel ? ' ' : '') + (item.metricLabel || '').trim()) + '</span>' : '') +
                  '</div>'
                : '';

            return '' +
                '<article class="catalog-promo-card theme-' + escapeHtml(item.theme) + '">' +
                    topMeta +
                    mediaMarkup +
                    (hasTitle ? '<h3>' + escapeHtml(item.title) + '</h3>' : '') +
                    (hasDescription ? '<p>' + escapeHtml(item.description) + '</p>' : '') +
                    (hasCta ? '<a href="' + escapeHtml(item.ctaUrl) + '" class="catalog-promo-link">' + escapeHtml(item.ctaText) + ' <i class="fas fa-arrow-right"></i></a>' : '') +
                '</article>';
        }).join('');

        band.hidden = false;
    }

    function stopHeroRotation() {
        if (state.heroTimer) {
            window.clearInterval(state.heroTimer);
            state.heroTimer = null;
        }
    }

    function startHeroRotation() {
        stopHeroRotation();
        if (state.heroItems.length < 2) {
            return;
        }

        state.heroTimer = window.setInterval(function () {
            moveHero(1, false);
        }, ROTATION_INTERVAL);
    }

    function moveHero(step, restartTimer) {
        if (!state.heroItems.length) {
            return;
        }

        state.heroIndex = (state.heroIndex + step + state.heroItems.length) % state.heroItems.length;
        renderHeroSlide();
        if (restartTimer !== false) {
            startHeroRotation();
        }
    }

    async function loadBanners() {
        try {
            const response = await window.fetch(getDataSource(), {
                headers: { Accept: 'application/json' },
                cache: 'no-store'
            });
            if (!response.ok) {
                throw new Error('No se pudo cargar la configuracion de campanas');
            }

            return withFallback(await response.json());
        } catch (error) {
            return withFallback([]);
        }
    }

    async function refresh() {
        const allItems = await loadBanners();
        const nextHeroItems = getPlacementItems(allItems, PLACEMENTS.hero);
        const nextCatalogItems = getPlacementItems(allItems, PLACEMENTS.catalog);
        const heroSignature = buildSignature(nextHeroItems);
        const catalogSignature = buildSignature(nextCatalogItems);

        if (heroSignature !== state.heroSignature) {
            state.heroItems = nextHeroItems;
            state.heroSignature = heroSignature;
            state.heroIndex = Math.min(state.heroIndex, Math.max(state.heroItems.length - 1, 0));
            renderHeroSlide();
            startHeroRotation();
        }

        if (catalogSignature !== state.catalogSignature) {
            state.catalogItems = nextCatalogItems;
            state.catalogSignature = catalogSignature;
            renderCatalogCards();
        }
    }

    function bindEvents() {
        document.addEventListener('click', function (event) {
            const arrow = event.target.closest('[data-home-campaign]');
            if (arrow) {
                moveHero(arrow.dataset.homeCampaign === 'prev' ? -1 : 1, true);
                return;
            }

            const indicator = event.target.closest('[data-home-indicator]');
            if (indicator) {
                const nextIndex = Number(indicator.dataset.homeIndicator);
                if (!Number.isNaN(nextIndex)) {
                    state.heroIndex = nextIndex;
                    renderHeroSlide();
                    startHeroRotation();
                }
            }
        });
    }

    function dispose() {
        stopHeroRotation();
        if (state.refreshTimer) {
            window.clearInterval(state.refreshTimer);
            state.refreshTimer = null;
        }
    }

    function init() {
        if (!document.getElementById('campaignBillboardViewport')) {
            return;
        }

        bindEvents();
        refresh();
        state.refreshTimer = window.setInterval(refresh, REFRESH_INTERVAL);
        window.addEventListener('beforeunload', dispose, { once: true });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init, { once: true });
    } else {
        init();
    }
})();