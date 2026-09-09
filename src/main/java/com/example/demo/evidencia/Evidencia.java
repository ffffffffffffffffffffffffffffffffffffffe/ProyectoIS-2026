package com.example.demo.evidencia;

import com.example.demo.portafolio.Portafolio;
import com.example.demo.usuario.Usuario;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "evidencias")
public class Evidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portafolio_id", nullable = false)
    private Portafolio portafolio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subido_por_id", nullable = false)
    private Usuario subidoPor;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(length = 1000)
    private String descripcion;

    @Column(nullable = false, length = 150)
    private String actividad;

    @Column(name = "nombre_original", nullable = false, length = 255)
    private String nombreOriginal;

    @Column(name = "clave_almacenamiento",
            nullable = false, unique = true, length = 100)
    private String claveAlmacenamiento;

    @Column(name = "tipo_contenido", nullable = false, length = 150)
    private String tipoContenido;

    @Column(name = "tamano_bytes", nullable = false)
    private long tamanoBytes;

    @Column(name = "fecha_subida", nullable = false, updatable = false)
    private LocalDateTime fechaSubida;

    protected Evidencia() {
    }

    public Evidencia(
            Portafolio portafolio,
            Usuario subidoPor,
            String titulo,
            String descripcion,
            String actividad,
            String nombreOriginal,
            String claveAlmacenamiento,
            String tipoContenido,
            long tamanoBytes) {
        this.portafolio = portafolio;
        this.subidoPor = subidoPor;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.actividad = actividad;
        this.nombreOriginal = nombreOriginal;
        this.claveAlmacenamiento = claveAlmacenamiento;
        this.tipoContenido = tipoContenido;
        this.tamanoBytes = tamanoBytes;
    }

    @PrePersist
    private void registrarFechaSubida() {
        this.fechaSubida = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Portafolio getPortafolio() {
        return portafolio;
    }

    public Usuario getSubidoPor() {
        return subidoPor;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getActividad() {
        return actividad;
    }

    public String getNombreOriginal() {
        return nombreOriginal;
    }

    public String getClaveAlmacenamiento() {
        return claveAlmacenamiento;
    }

    public String getTipoContenido() {
        return tipoContenido;
    }

    public long getTamanoBytes() {
        return tamanoBytes;
    }

    public LocalDateTime getFechaSubida() {
        return fechaSubida;
    }
}