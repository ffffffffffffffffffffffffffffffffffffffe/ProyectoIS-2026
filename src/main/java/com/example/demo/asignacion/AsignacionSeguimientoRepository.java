package com.example.demo.asignacion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AsignacionSeguimientoRepository
        extends JpaRepository<AsignacionSeguimiento, Long> {

    Optional<AsignacionSeguimiento>
    findByEstudiante_IdAndResponsable_IdAndTipoRelacionAndAsignaturaAndPeriodo(
            Long estudianteId,
            Long responsableId,
            TipoRelacion tipoRelacion,
            String asignatura,
            String periodo
    );

    List<AsignacionSeguimiento>
    findByResponsable_IdAndActivoTrue(Long responsableId);

    List<AsignacionSeguimiento>
    findByEstudiante_IdAndActivoTrue(Long estudianteId);
}