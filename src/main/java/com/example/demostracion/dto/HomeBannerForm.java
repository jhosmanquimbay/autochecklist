package com.example.demostracion.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class HomeBannerForm {

    private String id;

    @Size(max = 40, message = "La etiqueta superior no puede superar los 40 caracteres.")
    private String eyebrow;

    @Size(max = 80, message = "El título no puede superar los 80 caracteres.")
    private String title;

    @Size(max = 180, message = "El texto de apoyo no puede superar los 180 caracteres.")
    private String description;

    @Size(min = 1, message = "Selecciona una ubicación.")
    @Pattern(regexp = "hero|catalog|client", message = "Selecciona una ubicación válida.")
    private String placement = "hero";

    @Size(max = 30, message = "El texto del botón no puede superar los 30 caracteres.")
    private String ctaText;

    @Size(max = 255, message = "El enlace no puede superar los 255 caracteres.")
    private String ctaUrl;

    @Size(max = 12, message = "La métrica principal no puede superar los 12 caracteres.")
    private String metricValue;

    @Size(max = 24, message = "La etiqueta de la métrica no puede superar los 24 caracteres.")
    private String metricLabel;

    @Pattern(regexp = "gold|blue|emerald", message = "Selecciona un tema válido.")
    private String theme = "gold";

    @Size(max = 60, message = "El icono no puede superar los 60 caracteres.")
    private String icon = "fas fa-bullhorn";

    @Size(max = 255, message = "La ruta de la imagen no puede superar los 255 caracteres.")
    private String imageUrl;

    private boolean active = true;

    @JsonIgnore
    private boolean removeImage;

    @Min(value = 1, message = "El orden mínimo es 1.")
    @Max(value = 99, message = "El orden máximo es 99.")
    private Integer displayOrder;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEyebrow() {
        return eyebrow;
    }

    public void setEyebrow(String eyebrow) {
        this.eyebrow = eyebrow;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPlacement() {
        return placement;
    }

    public void setPlacement(String placement) {
        this.placement = placement;
    }

    public String getCtaText() {
        return ctaText;
    }

    public void setCtaText(String ctaText) {
        this.ctaText = ctaText;
    }

    public String getCtaUrl() {
        return ctaUrl;
    }

    public void setCtaUrl(String ctaUrl) {
        this.ctaUrl = ctaUrl;
    }

    public String getMetricValue() {
        return metricValue;
    }

    public void setMetricValue(String metricValue) {
        this.metricValue = metricValue;
    }

    public String getMetricLabel() {
        return metricLabel;
    }

    public void setMetricLabel(String metricLabel) {
        this.metricLabel = metricLabel;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isRemoveImage() {
        return removeImage;
    }

    public void setRemoveImage(boolean removeImage) {
        this.removeImage = removeImage;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    @JsonIgnore
    public String getPlacementLabel() {
        return switch (placement) {
            case "catalog" -> "Promoción home";
            case "client" -> "Portal cliente";
            default -> "Carrusel principal";
        };
    }

    @JsonIgnore
    public boolean hasImage() {
        return imageUrl != null && !imageUrl.isBlank();
    }
}