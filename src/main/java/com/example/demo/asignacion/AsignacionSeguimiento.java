package com.example.demo.asignacion;

import com.example.demo.usuario.Usuario;
import jakarta.persistence.*;

@Entity
@Table(
        name = "asignaciones_seguimiento",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_asignacion_contexto",
                columnNames = {
                        "estudiante_id",
                        "responsable_id",
                        "tipo_relacion",
                        "asignatura",
                        "periodo"
                }
        )
)
public class AsignacionSeguimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Usuario estudiante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsable_id", nullable = false)
    private Usuario responsable;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_relacion", nullable = false, length = 30)
    private TipoRelacion tipoRelacion;

    @Column(nullable = false, length = 100)
    private String asignatura;

    @Column(nullable = false, length = 20)
    private String periodo;

    @Column(nullable = false)
    private boolean activo = true;

    protected AsignacionSeguimiento() {
    }

    public AsignacionSeguimiento(
            Usuario estudiante,
            Usuario responsable,
            TipoRelacion tipoRelacion,
            String asignatura,
            String periodo) {
        this.estudiante = estudiante;
        this.responsable = responsable;
        this.tipoRelacion = tipoRelacion;
        this.asignatura = asignatura;
        this.periodo = periodo;
    }

    public Long getId() {
        return id;
    }

    public Usuario getEstudiante() {
        return estudiante;
    }

    public Usuario getResponsable() {
        return responsable;
    }

    public TipoRelacion getTipoRelacion() {
        return tipoRelacion;
    }

    public String getAsignatura() {
        return asignatura;
    }

    public String getPeriodo() {
        return periodo;
    }

    public boolean isActivo() {
        return activo;
    }

    public void activar() {
        activo = true;
    }

    public void desactivar() {
        activo = false;
    }
}