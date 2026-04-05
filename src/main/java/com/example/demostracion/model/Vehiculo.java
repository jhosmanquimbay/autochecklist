package com.example.demostracion.model;

import java.time.LocalDateTime;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "vehiculo")
public class Vehiculo {

    public static final String ESTADO_PUBLICACION_BORRADOR = "BORRADOR";
    public static final String ESTADO_PUBLICACION_PENDIENTE = "PENDIENTE";
    public static final String ESTADO_PUBLICACION_PUBLICADO = "PUBLICADO";
    public static final String ESTADO_PUBLICACION_DEVUELTO = "DEVUELTO";
    public static final String ESTADO_PUBLICACION_DESPUBLICADO = "DESPUBLICADO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idVehiculo")
    private Long idVehiculo;

    @Column(nullable = false, length = 100, unique = true)
    private String chasis;

    @Column(nullable = false, length = 100)
    private String modelo;

    @Column(nullable = false, length = 100)
    private String marca;

    private Integer anio;

    private Double precio;

    @Column(length = 50)
    private String cilindrada;

    @Column(length = 50)
    private String tipoCombustible;

    @Column(length = 50)
    private String transmision;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    /* ===== IMAGEN ===== */
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "LONGBLOB")
    private byte[] imagen;

    @Column(name = "ImagenUrl", length = 255)
    private String imagenUrl;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "imagen2", columnDefinition = "LONGBLOB")
    private byte[] imagen2;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "imagen3", columnDefinition = "LONGBLOB")
    private byte[] imagen3;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "imagen4", columnDefinition = "LONGBLOB")
    private byte[] imagen4;

    @Column(name = "especificacionesTecnicas", columnDefinition = "TEXT")
    private String especificacionesTecnicas;

    @Column(name = "popularidad", nullable = false)
    private Integer popularidad = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "estado_publicacion", nullable = false, length = 30)
    private String estadoPublicacion = ESTADO_PUBLICACION_BORRADOR;

    @Column(name = "fecha_publicacion")
    private LocalDateTime fechaPublicacion;

    // BD externa sin FK inventario->vehiculo: campo no persistente para compatibilidad.
    @Transient
    private Inventario inventario;

    @Transient
    private Long inventarioSeleccionadoId;

    // ===== CONSTRUCTOR =====
    public Vehiculo() {
        this.fechaCreacion = LocalDateTime.now();
    }

    // ===== GETTERS & SETTERS =====

    public Long getIdVehiculo() {
        return idVehiculo;
    }

    public void setIdVehiculo(Long idVehiculo) {
        this.idVehiculo = idVehiculo;
    }

    public String getChasis() {
        return chasis;
    }

    public void setChasis(String chasis) {
        this.chasis = chasis;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public String getCilindrada() {
        return cilindrada;
    }

    public void setCilindrada(String cilindrada) {
        this.cilindrada = cilindrada;
    }

    public String getTipoCombustible() {
        return tipoCombustible;
    }

    public void setTipoCombustible(String tipoCombustible) {
        this.tipoCombustible = tipoCombustible;
    }

    public String getTransmision() {
        return transmision;
    }

    public void setTransmision(String transmision) {
        this.transmision = transmision;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public byte[] getImagen() {
        return imagen;
    }

    public void setImagen(byte[] imagen) {
        this.imagen = imagen;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public byte[] getImagen2() {
        return imagen2;
    }

    public void setImagen2(byte[] imagen2) {
        this.imagen2 = imagen2;
    }

    public byte[] getImagen3() {
        return imagen3;
    }

    public void setImagen3(byte[] imagen3) {
        this.imagen3 = imagen3;
    }

    public byte[] getImagen4() {
        return imagen4;
    }

    public void setImagen4(byte[] imagen4) {
        this.imagen4 = imagen4;
    }

    public String getEspecificacionesTecnicas() {
        return especificacionesTecnicas;
    }

    public void setEspecificacionesTecnicas(String especificacionesTecnicas) {
        this.especificacionesTecnicas = especificacionesTecnicas;
    }

    public Integer getPopularidad() {
        return popularidad;
    }

    public void setPopularidad(Integer popularidad) {
        this.popularidad = popularidad;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Inventario getInventario() {
        return inventario;
    }

    public void setInventario(Inventario inventario) {
        this.inventario = inventario;
    }

    public Long getInventarioSeleccionadoId() {
        return inventarioSeleccionadoId;
    }

    public void setInventarioSeleccionadoId(Long inventarioSeleccionadoId) {
        this.inventarioSeleccionadoId = inventarioSeleccionadoId;
    }

    public String getEstadoPublicacion() {
        if (estadoPublicacion == null || estadoPublicacion.isBlank()) {
            return ESTADO_PUBLICACION_BORRADOR;
        }
        return estadoPublicacion;
    }

    public void setEstadoPublicacion(String estadoPublicacion) {
        if (estadoPublicacion == null || estadoPublicacion.isBlank()) {
            this.estadoPublicacion = ESTADO_PUBLICACION_BORRADOR;
            return;
        }
        this.estadoPublicacion = estadoPublicacion.trim().toUpperCase();
    }

    public LocalDateTime getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(LocalDateTime fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public boolean estaPublicado() {
        return ESTADO_PUBLICACION_PUBLICADO.equalsIgnoreCase(getEstadoPublicacion());
    }

    public boolean estaPendientePublicacion() {
        return ESTADO_PUBLICACION_PENDIENTE.equalsIgnoreCase(getEstadoPublicacion());
    }

    public String getEstadoPublicacionEtiqueta() {
        return switch (getEstadoPublicacion()) {
            case ESTADO_PUBLICACION_PENDIENTE -> "Pendiente de publicación";
            case ESTADO_PUBLICACION_PUBLICADO -> "Publicado";
            case ESTADO_PUBLICACION_DEVUELTO -> "Devuelto por admin";
            case ESTADO_PUBLICACION_DESPUBLICADO -> "Despublicado";
            default -> "Borrador";
        };
    }
}