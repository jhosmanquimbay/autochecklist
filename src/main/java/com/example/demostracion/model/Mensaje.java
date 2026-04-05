package com.example.demostracion.model;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "mensaje")
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "remitente_id", referencedColumnName = "id_usuario")
    private Usuario remitente;

    @ManyToOne
    @JoinColumn(name = "destinatario_id", referencedColumnName = "id_usuario")
    private Usuario destinatario;

    @Column(name = "destinatario_externo")
    private String destinatarioExterno;

    private String asunto;

    @Column(columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    private boolean leido = false;

    private String carpeta;

    @Column(name = "eliminado")
    private boolean eliminado = false;

    @Column(name = "eliminado_permanente")
    private boolean eliminadoPermanente = false;

    @Column(name = "id_padre")
    private Long idPadre;

    @OneToMany(mappedBy = "mensaje",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @JsonIgnoreProperties("mensaje")
    private List<Adjunto> adjuntos;

    @PrePersist
    public void prePersist() {
        this.fechaEnvio = LocalDateTime.now();
    }

        // GETTERS Y SETTERS
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Usuario getRemitente() { return remitente; }
        public void setRemitente(Usuario remitente) { this.remitente = remitente; }

        public Usuario getDestinatario() { return destinatario; }
        public void setDestinatario(Usuario destinatario) { this.destinatario = destinatario; }

        public String getDestinatarioExterno() { return destinatarioExterno; }
        public void setDestinatarioExterno(String destinatarioExterno) { this.destinatarioExterno = destinatarioExterno; }

        public String getAsunto() { return asunto; }
        public void setAsunto(String asunto) { this.asunto = asunto; }

        public String getContenido() { return contenido; }
        public void setContenido(String contenido) { this.contenido = contenido; }

        public LocalDateTime getFechaEnvio() { return fechaEnvio; }
        public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }

        public boolean isLeido() { return leido; }
        public void setLeido(boolean leido) { this.leido = leido; }

        public String getCarpeta() { return carpeta; }
        public void setCarpeta(String carpeta) { this.carpeta = carpeta; }

        public boolean isEliminado() { return eliminado; }
        public void setEliminado(boolean eliminado) { this.eliminado = eliminado; }

        public boolean isEliminadoPermanente() { return eliminadoPermanente; }
        public void setEliminadoPermanente(boolean eliminadoPermanente) { this.eliminadoPermanente = eliminadoPermanente; }

        public Long getIdPadre() { return idPadre; }
        public void setIdPadre(Long idPadre) { this.idPadre = idPadre; }

        public List<Adjunto> getAdjuntos() { return adjuntos; }
        public void setAdjuntos(List<Adjunto> adjuntos) { this.adjuntos = adjuntos; }
    }
