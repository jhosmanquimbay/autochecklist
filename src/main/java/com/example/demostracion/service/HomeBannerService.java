package com.example.demostracion.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demostracion.dto.HomeBannerForm;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Service
public class HomeBannerService {

    private static final String DEFAULT_RESOURCE = "classpath:static/home-banners.json";
    private static final String DEFAULT_IMAGE_DIR = "uploads/home-banners";
    private static final String IMAGE_URL_PREFIX = "/uploads/home-banners/";
    private static final long MAX_IMAGE_SIZE_BYTES = 5L * 1024L * 1024L;
    private static final Comparator<HomeBannerForm> ORDER_COMPARATOR = Comparator
            .comparingInt((HomeBannerForm banner) -> placementRank(banner.getPlacement()))
            .thenComparing((left, right) -> Integer.compare(safeDisplayOrder(left), safeDisplayOrder(right)))
            .thenComparing(HomeBannerForm::getTitle, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));

    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    private final Path storagePath;
    private final Path imageStorageDir;

    @Autowired
    public HomeBannerService(ResourceLoader resourceLoader,
                             ObjectMapper objectMapper,
                             @Value("${app.home-banners.storage-path:uploads/config/home-banners-admin.json}") String storagePath,
                             @Value("${app.home-banners.image-dir:" + DEFAULT_IMAGE_DIR + "}") String imageStorageDir) {
        this(resourceLoader, objectMapper, resolveStoragePath(storagePath), resolveStoragePath(imageStorageDir));
    }

    HomeBannerService(ResourceLoader resourceLoader, ObjectMapper objectMapper, Path storagePath, Path imageStorageDir) {
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper.copy().enable(SerializationFeature.INDENT_OUTPUT);
        this.storagePath = storagePath;
        this.imageStorageDir = imageStorageDir;
    }

    public synchronized List<HomeBannerForm> listarTodos() {
        return copiarLista(cargarOrdenados());
    }

    public synchronized List<HomeBannerForm> listarActivos() {
        return cargarOrdenados().stream()
                .filter(HomeBannerForm::isActive)
                .map(this::copiar)
                .toList();
    }

    public synchronized HomeBannerForm obtenerPorId(String id) {
        return copiar(encontrarPorId(cargarOrdenados(), id));
    }

    public synchronized HomeBannerForm crear(HomeBannerForm form) {
        return crear(form, null);
    }

    public synchronized HomeBannerForm crear(HomeBannerForm form, MultipartFile imageFile) {
        List<HomeBannerForm> banners = cargarOrdenados();
        HomeBannerForm nuevo = normalizar(form);
        nuevo.setId(UUID.randomUUID().toString());
        nuevo.setImageUrl(guardarImagenSiExiste(imageFile, null));
        if (nuevo.getDisplayOrder() == null) {
            nuevo.setDisplayOrder(siguienteOrden(banners));
        }
        banners.add(nuevo);
        guardar(banners);
        return copiar(nuevo);
    }

    public synchronized HomeBannerForm actualizar(String id, HomeBannerForm form) {
        return actualizar(id, form, null);
    }

    public synchronized HomeBannerForm actualizar(String id, HomeBannerForm form, MultipartFile imageFile) {
        List<HomeBannerForm> banners = cargarOrdenados();
        int index = indicePorId(banners, id);
        HomeBannerForm existente = banners.get(index);
        HomeBannerForm actualizado = normalizar(form);
        actualizado.setId(id);
        actualizado.setImageUrl(resolverImagenActualizada(form, imageFile, existente.getImageUrl()));
        if (actualizado.getDisplayOrder() == null) {
            actualizado.setDisplayOrder(existente.getDisplayOrder());
        }
        banners.set(index, actualizado);
        guardar(banners);
        return copiar(actualizado);
    }

    public synchronized void cambiarEstadoPublicacion(String id) {
        List<HomeBannerForm> banners = cargarOrdenados();
        HomeBannerForm banner = banners.get(indicePorId(banners, id));
        banner.setActive(!banner.isActive());
        guardar(banners);
    }

    public synchronized void eliminar(String id) {
        List<HomeBannerForm> banners = cargarOrdenados();
        int index = indicePorId(banners, id);
        HomeBannerForm eliminado = banners.remove(index);
        eliminarImagenGestionada(eliminado.getImageUrl());
        guardar(banners);
    }

    private List<HomeBannerForm> cargarOrdenados() {
        List<HomeBannerForm> banners = cargar();
        banners.sort(ORDER_COMPARATOR);
        return banners;
    }

    private List<HomeBannerForm> cargar() {
        try {
            asegurarArchivo();
            JavaType listType = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, HomeBannerForm.class);
            try (Reader reader = Files.newBufferedReader(storagePath, StandardCharsets.UTF_8)) {
                List<HomeBannerForm> banners = objectMapper.readValue(reader, listType);
                if (banners == null) {
                    return new ArrayList<>();
                }

                List<HomeBannerForm> normalizados = new ArrayList<>();
                int orden = 1;
                for (HomeBannerForm banner : banners) {
                    HomeBannerForm limpio = normalizarPersistido(banner, orden++);
                    normalizados.add(limpio);
                }
                return normalizados;
            }
        } catch (IOException ex) {
            throw new IllegalStateException("No fue posible leer las promociones del inicio.", ex);
        }
    }

    private void guardar(List<HomeBannerForm> banners) {
        try {
            if (storagePath.getParent() != null) {
                Files.createDirectories(storagePath.getParent());
            }

            List<HomeBannerForm> ordenados = banners.stream()
                    .map(this::copiar)
                    .sorted(ORDER_COMPARATOR)
                    .toList();

            try (Writer writer = Files.newBufferedWriter(
                    storagePath,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE)) {
                objectMapper.writeValue(writer, ordenados);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("No fue posible guardar las promociones del inicio.", ex);
        }
    }

    private void asegurarArchivo() throws IOException {
        if (storagePath.getParent() != null) {
            Files.createDirectories(storagePath.getParent());
        }

        if (!Files.exists(storagePath)) {
            guardar(cargarPredeterminados());
        }
    }

    private List<HomeBannerForm> cargarPredeterminados() {
        Resource resource = resourceLoader.getResource(DEFAULT_RESOURCE);
        if (resource.exists()) {
            JavaType listType = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, SeedBanner.class);
            try (InputStream inputStream = resource.getInputStream()) {
                List<SeedBanner> seeds = objectMapper.readValue(inputStream, listType);
                if (seeds != null && !seeds.isEmpty()) {
                    List<HomeBannerForm> banners = new ArrayList<>();
                    int orden = 1;
                    for (SeedBanner seed : seeds) {
                        HomeBannerForm banner = new HomeBannerForm();
                        banner.setId(UUID.randomUUID().toString());
                        banner.setEyebrow(textOrDefault(seed.eyebrow, "Campaña activa"));
                        banner.setTitle(textOrDefault(seed.title, "Promoción destacada"));
                        banner.setDescription(textOrDefault(seed.description, defaultDescription(validPlacement(seed.placement))));
                        banner.setPlacement(validPlacement(seed.placement));
                        banner.setCtaText(textOrDefault(seed.ctaText, "Ver catálogo"));
                        banner.setCtaUrl(textOrDefault(seed.ctaUrl, "#vehiculos"));
                        banner.setMetricValue(textOrDefault(seed.metricValue, "24/7"));
                        banner.setMetricLabel(textOrDefault(seed.metricLabel, "activo"));
                        banner.setTheme(validTheme(seed.theme, orden));
                        banner.setIcon(textOrDefault(seed.icon, "fas fa-bullhorn"));
                        banner.setImageUrl(textOrNull(seed.imageUrl));
                        banner.setActive(true);
                        banner.setDisplayOrder(orden++);
                        banners.add(banner);
                    }
                    return banners;
                }
            } catch (IOException ignored) {
            }
        }

        return bannersFallback();
    }

    private List<HomeBannerForm> bannersFallback() {
        List<HomeBannerForm> banners = new ArrayList<>();
        banners.add(nuevoBannerFallback(1, "hero", "Lanzamiento activo", "SUVs con entrega inmediata", "Promocion principal con imagen protagonista para comunicar ofertas fuertes sin recargar la pagina.", "Explorar inventario", "#vehiculos", "48h", "respuesta", "gold", "fas fa-car-side"));
        banners.add(nuevoBannerFallback(2, "hero", "Financiacion", "Promociones de temporada con imagen editable", "Muestra una pieza visual grande, cambia la foto desde admin y manten la portada siempre actualizada.", "Ver promociones", "#catalogPromoBand", "0%", "entrada", "blue", "fas fa-percent"));
        banners.add(nuevoBannerFallback(1, "catalog", "Promocion home", "Agenda una prueba de manejo", "Tarjeta secundaria para ofertas o llamados concretos antes de entrar al catalogo.", "Reservar ahora", "#contacto", "15m", "confirmacion", "gold", "fas fa-calendar-check"));
        banners.add(nuevoBannerFallback(2, "catalog", "Beneficio vigente", "Modelos con bono especial", "Campana puntual dentro del home para apoyar una categoria o una oferta temporal.", "Ver destacados", "#destacados", "Top", "consulta", "emerald", "fas fa-award"));
        banners.add(nuevoBannerFallback(1, "client", "Portal cliente", "Promociones visibles tambien para tus clientes", "El portal cliente muestra un carrusel compacto con imagen y boton para destacar beneficios activos.", "Ir a favoritos", "/cliente/favoritos", "VIP", "beneficio", "blue", "fas fa-user-shield"));
        banners.add(nuevoBannerFallback(2, "client", "Seguimiento", "Activa mensajes para compras y perfil", "Usa esta ubicacion para campanas de fidelizacion, bonos o recordatorios dentro del portal.", "Ir a compras", "/cliente/compras", "3x", "impacto", "emerald", "fas fa-gem"));
        return banners;
    }

    private HomeBannerForm nuevoBannerFallback(int orden,
                                               String placement,
                                               String eyebrow,
                                               String title,
                                               String description,
                                               String ctaText,
                                               String ctaUrl,
                                               String metricValue,
                                               String metricLabel,
                                               String theme,
                                               String icon) {
        HomeBannerForm banner = new HomeBannerForm();
        banner.setId(UUID.randomUUID().toString());
        banner.setEyebrow(eyebrow);
        banner.setTitle(title);
        banner.setDescription(description);
        banner.setPlacement(validPlacement(placement));
        banner.setCtaText(ctaText);
        banner.setCtaUrl(ctaUrl);
        banner.setMetricValue(metricValue);
        banner.setMetricLabel(metricLabel);
        banner.setTheme(theme);
        banner.setIcon(icon);
        banner.setImageUrl(null);
        banner.setActive(true);
        banner.setDisplayOrder(orden);
        return banner;
    }

    private HomeBannerForm normalizar(HomeBannerForm source) {
        HomeBannerForm safeSource = source != null ? source : new HomeBannerForm();
        Integer displayOrder = safeSource.getDisplayOrder();
        Integer normalizedOrder = displayOrder != null && displayOrder > 0 ? displayOrder : null;
        String validatedPlacement = validPlacement(safeSource.getPlacement());
        HomeBannerForm banner = new HomeBannerForm();
        banner.setEyebrow(textOrNull(safeSource.getEyebrow()));
        banner.setTitle(textOrNull(safeSource.getTitle()));
        banner.setDescription(textOrNull(safeSource.getDescription()));
        banner.setPlacement(validatedPlacement);
        banner.setCtaText(textOrNull(safeSource.getCtaText()));
        banner.setCtaUrl(textOrNull(safeSource.getCtaUrl()));
        banner.setMetricValue(textOrNull(safeSource.getMetricValue()));
        banner.setMetricLabel(textOrNull(safeSource.getMetricLabel()));
        banner.setTheme(validTheme(safeSource.getTheme(), normalizedOrder != null ? normalizedOrder : 1));
        banner.setIcon(textOrDefault(safeSource.getIcon(), "fas fa-bullhorn"));
        banner.setImageUrl(textOrNull(safeSource.getImageUrl()));
        banner.setActive(safeSource.isActive());
        banner.setRemoveImage(false);
        banner.setDisplayOrder(normalizedOrder);
        return banner;
    }

    private HomeBannerForm normalizarPersistido(HomeBannerForm source, int fallbackOrder) {
        HomeBannerForm banner = normalizar(source != null ? source : new HomeBannerForm());
        String trimmedSourceId = source != null && source.getId() != null ? source.getId().trim() : null;
        if (hasText(trimmedSourceId)) {
            banner.setId(trimmedSourceId);
        } else {
            banner.setId(UUID.randomUUID().toString());
        }
        if (banner.getDisplayOrder() == null || banner.getDisplayOrder() < 1) {
            banner.setDisplayOrder(fallbackOrder);
        }
        limpiarContenidoAutogenerado(banner);
        return banner;
    }

    private void limpiarContenidoAutogenerado(HomeBannerForm banner) {
        if (banner == null) {
            return;
        }

        if (Objects.equals(banner.getEyebrow(), "Campaña activa")) {
            banner.setEyebrow(null);
        }
        if (Objects.equals(banner.getTitle(), "Promoción destacada")) {
            banner.setTitle(null);
        }
        if (Objects.equals(banner.getDescription(), defaultDescription(banner.getPlacement()))) {
            banner.setDescription(null);
        }
        if (Objects.equals(banner.getCtaText(), "Ver catálogo") && Objects.equals(banner.getCtaUrl(), "#vehiculos")) {
            banner.setCtaText(null);
            banner.setCtaUrl(null);
        }
        if (Objects.equals(banner.getMetricValue(), "24/7") && Objects.equals(banner.getMetricLabel(), "activo")) {
            banner.setMetricValue(null);
            banner.setMetricLabel(null);
        }
    }

    private HomeBannerForm copiar(HomeBannerForm source) {
        HomeBannerForm banner = new HomeBannerForm();
        banner.setId(source.getId());
        banner.setEyebrow(source.getEyebrow());
        banner.setTitle(source.getTitle());
        banner.setDescription(source.getDescription());
        banner.setPlacement(source.getPlacement());
        banner.setCtaText(source.getCtaText());
        banner.setCtaUrl(source.getCtaUrl());
        banner.setMetricValue(source.getMetricValue());
        banner.setMetricLabel(source.getMetricLabel());
        banner.setTheme(source.getTheme());
        banner.setIcon(source.getIcon());
        banner.setImageUrl(source.getImageUrl());
        banner.setActive(source.isActive());
        banner.setDisplayOrder(source.getDisplayOrder());
        return banner;
    }

    private String resolverImagenActualizada(HomeBannerForm form, MultipartFile imageFile, String currentImageUrl) {
        boolean hasNewImage = imageFile != null && !imageFile.isEmpty();
        if (hasNewImage) {
            return guardarImagen(imageFile, currentImageUrl);
        }

        if (form != null && form.isRemoveImage()) {
            eliminarImagenGestionada(currentImageUrl);
            return null;
        }

        return textOrNull(currentImageUrl);
    }

    private String guardarImagenSiExiste(MultipartFile imageFile, String currentImageUrl) {
        if (imageFile == null || imageFile.isEmpty()) {
            return textOrNull(currentImageUrl);
        }
        return guardarImagen(imageFile, currentImageUrl);
    }

    private String guardarImagen(MultipartFile imageFile, String currentImageUrl) {
        validarImagen(imageFile);
        String extension = obtenerExtensionPermitida(imageFile);

        try {
            Files.createDirectories(imageStorageDir);
            String fileName = UUID.randomUUID() + extension;
            Path destination = imageStorageDir.resolve(fileName).normalize();
            if (!destination.startsWith(imageStorageDir)) {
                throw new IllegalStateException("La ruta de la imagen no es válida.");
            }

            try (InputStream inputStream = imageFile.getInputStream()) {
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }

            eliminarImagenGestionada(currentImageUrl);
            return IMAGE_URL_PREFIX + fileName;
        } catch (IOException ex) {
            throw new IllegalStateException("No fue posible guardar la imagen de la promoción.", ex);
        }
    }

    private void validarImagen(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("Selecciona una imagen válida.");
        }

        if (imageFile.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new IllegalArgumentException("La imagen no puede superar los 5 MB.");
        }

        String contentType = textOrNull(imageFile.getContentType());
        if (contentType != null && !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new IllegalArgumentException("Solo se permiten archivos de imagen.");
        }

        obtenerExtensionPermitida(imageFile);
    }

    private String obtenerExtensionPermitida(MultipartFile imageFile) {
        String originalName = textOrNull(imageFile.getOriginalFilename());
        if (originalName == null) {
            throw new IllegalArgumentException("El archivo seleccionado no tiene un nombre válido.");
        }

        String normalized = originalName.toLowerCase(Locale.ROOT);
        if (normalized.endsWith(".jpg") || normalized.endsWith(".jpeg")) {
            return normalized.endsWith(".jpeg") ? ".jpeg" : ".jpg";
        }
        if (normalized.endsWith(".png")) {
            return ".png";
        }
        if (normalized.endsWith(".webp")) {
            return ".webp";
        }
        if (normalized.endsWith(".gif")) {
            return ".gif";
        }

        throw new IllegalArgumentException("La imagen debe estar en formato JPG, PNG, WEBP o GIF.");
    }

    private void eliminarImagenGestionada(String imageUrl) {
        String normalizedUrl = textOrNull(imageUrl);
        if (normalizedUrl == null || !normalizedUrl.startsWith(IMAGE_URL_PREFIX)) {
            return;
        }

        String fileName = normalizedUrl.substring(IMAGE_URL_PREFIX.length());
        if (!hasText(fileName)) {
            return;
        }

        Path target = imageStorageDir.resolve(fileName).normalize();
        if (!target.startsWith(imageStorageDir)) {
            return;
        }

        try {
            Files.deleteIfExists(target);
        } catch (IOException ex) {
            throw new IllegalStateException("No fue posible eliminar la imagen anterior de la promoción.", ex);
        }
    }

    private List<HomeBannerForm> copiarLista(List<HomeBannerForm> source) {
        return source.stream().map(this::copiar).toList();
    }

    private HomeBannerForm encontrarPorId(List<HomeBannerForm> banners, String id) {
        return banners.stream()
                .filter(banner -> Objects.equals(banner.getId(), id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("La promoción solicitada no existe."));
    }

    private int indicePorId(List<HomeBannerForm> banners, String id) {
        for (int index = 0; index < banners.size(); index++) {
            if (Objects.equals(banners.get(index).getId(), id)) {
                return index;
            }
        }
        throw new IllegalArgumentException("La promoción solicitada no existe.");
    }

    private int siguienteOrden(List<HomeBannerForm> banners) {
        return banners.stream()
                .map(HomeBannerForm::getDisplayOrder)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }

    private static Path resolveStoragePath(String storagePath) {
        Path path = Paths.get(storagePath);
        if (!path.isAbsolute()) {
            path = Paths.get("").toAbsolutePath().resolve(path);
        }
        return path.normalize();
    }

    private String textOrDefault(String value, String fallback) {
        String trimmed = value == null ? null : value.trim();
        return hasText(trimmed) ? trimmed : fallback;
    }

    private String textOrNull(String value) {
        String trimmed = value == null ? null : value.trim();
        return hasText(trimmed) ? trimmed : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static int placementRank(String placement) {
        return switch (validPlacement(placement)) {
            case "catalog" -> 1;
            case "client" -> 2;
            default -> 0;
        };
    }

    private static int safeDisplayOrder(HomeBannerForm banner) {
        Integer displayOrder = banner != null ? banner.getDisplayOrder() : null;
        return displayOrder != null ? displayOrder : Integer.MAX_VALUE;
    }

    private static String validPlacement(String placement) {
        if (placement == null) {
            return "hero";
        }

        String normalized = placement.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "catalog", "client" -> normalized;
            case "showcase" -> "catalog";
            default -> "hero";
        };
    }

    private String defaultDescription(String placement) {
        return switch (validPlacement(placement)) {
            case "catalog" -> "Bloque visual para destacar promociones puntuales dentro del home sin recargar el catálogo.";
            case "client" -> "Campaña visible dentro del portal cliente para resaltar promociones, beneficios o lanzamientos activos.";
            default -> "Campaña principal con presencia visual, mensaje limpio y llamada a la acción directa en la portada.";
        };
    }

    private String validTheme(String theme, int orderHint) {
        if (hasText(theme)) {
            String normalized = theme.trim().toLowerCase(Locale.ROOT);
            if (normalized.equals("gold") || normalized.equals("blue") || normalized.equals("emerald")) {
                return normalized;
            }
        }

        return switch (Math.floorMod(orderHint - 1, 3)) {
            case 1 -> "blue";
            case 2 -> "emerald";
            default -> "gold";
        };
    }

    private static class SeedBanner {
        public String eyebrow;
        public String title;
        public String description;
        public String placement;
        public String ctaText;
        public String ctaUrl;
        public String metricValue;
        public String metricLabel;
        public String theme;
        public String icon;
        public String imageUrl;
    }
}