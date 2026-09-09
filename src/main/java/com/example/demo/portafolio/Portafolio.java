package com.example.demo.portafolio;

import com.example.demo.usuario.Usuario;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "portafolios",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_portafolio_estudiante_contexto",
                columnNames = {
                        "estudiante_id",
                        "asignatura",
                        "periodo"
                }
        )
)
public class Portafolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Usuario estudiante;

    @Column(nullable = false, length = 100)
    private String asignatura;

    @Column(nullable = false, length = 20)
    private String periodo;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    protected Portafolio() {
    }

    public Portafolio(
            Usuario estudiante,
            String asignatura,
            String periodo) {
        this.estudiante = estudiante;
        this.asignatura = asignatura;
        this.periodo = periodo;
    }

    @PrePersist
    private void registrarFechaCreacion() {
        this.fechaCreacion = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Usuario getEstudiante() {
        return estudiante;
    }

    public String getAsignatura() {
        return asignatura;
    }

    public String getPeriodo() {
        return periodo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
}
