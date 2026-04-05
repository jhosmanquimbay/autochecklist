package com.example.demostracion.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.mock.web.MockMultipartFile;

import com.example.demostracion.dto.HomeBannerForm;
import com.fasterxml.jackson.databind.ObjectMapper;

class HomeBannerServiceTest {

    @TempDir
    Path tempDir;

    private Path storagePath;
    private Path imageStorageDir;
    private HomeBannerService homeBannerService;

    @BeforeEach
    void setUp() {
        storagePath = tempDir.resolve("home-banners-admin.json");
        imageStorageDir = tempDir.resolve("banner-images");
        homeBannerService = new HomeBannerService(new DefaultResourceLoader(), new ObjectMapper(), storagePath, imageStorageDir);
    }

    @Test
    void deberiaSembrarPromocionesPredeterminadasCuandoNoExisteArchivo() {
        assertThat(homeBannerService.listarTodos()).isNotEmpty();
        assertThat(Files.exists(storagePath)).isTrue();
        assertThat(homeBannerService.listarTodos()).allMatch(HomeBannerForm::isActive);
    }

    @Test
    void deberiaCrearYDespublicarPromocion() {
        HomeBannerForm banner = new HomeBannerForm();
        banner.setEyebrow("Lanzamiento");
        banner.setTitle("Nuevo banner");
        banner.setCtaText("Ver");
        banner.setCtaUrl("#vehiculos");
        banner.setMetricValue("7d");
        banner.setMetricLabel("vigencia");
        banner.setTheme("gold");
        banner.setIcon("fas fa-bullhorn");
        banner.setDisplayOrder(7);
        banner.setActive(true);

        HomeBannerForm creado = homeBannerService.crear(banner);
        assertThat(creado.getId()).isNotBlank();
        assertThat(homeBannerService.listarActivos()).extracting(HomeBannerForm::getTitle).contains("Nuevo banner");

        homeBannerService.cambiarEstadoPublicacion(creado.getId());

        assertThat(homeBannerService.listarActivos()).extracting(HomeBannerForm::getTitle).doesNotContain("Nuevo banner");
        assertThat(homeBannerService.obtenerPorId(creado.getId()).isActive()).isFalse();
    }

    @Test
    void deberiaGuardarImagenCuandoSeCreaPromocion() throws Exception {
        HomeBannerForm banner = new HomeBannerForm();
        banner.setEyebrow("Lanzamiento visual");
        banner.setTitle("Banner con foto");
        banner.setCtaText("Ver ahora");
        banner.setCtaUrl("#promociones");
        banner.setMetricValue("5d");
        banner.setMetricLabel("campaña");
        banner.setTheme("blue");
        banner.setIcon("fas fa-image");
        banner.setDisplayOrder(3);
        banner.setActive(true);

        MockMultipartFile imageFile = new MockMultipartFile(
                "imageFile",
                "promo.png",
                "image/png",
                new byte[] {1, 2, 3, 4, 5}
        );

        HomeBannerForm creado = homeBannerService.crear(banner, imageFile);

        assertThat(creado.getImageUrl()).startsWith("/uploads/home-banners/");
        assertThat(Files.list(imageStorageDir)).hasSize(1);
    }
}