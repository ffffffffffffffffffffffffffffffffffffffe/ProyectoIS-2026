package com.example.demo.portafolio;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortafolioRepository
        extends JpaRepository<Portafolio, Long> {

    List<Portafolio> findByEstudiante_IdOrderByFechaCreacionDesc(
            Long estudianteId
    );

    Optional<Portafolio> findByIdAndEstudiante_Id(
            Long id,
            Long estudianteId
    );

    boolean existsByEstudiante_IdAndAsignaturaAndPeriodo(
            Long estudianteId,
            String asignatura,
            String periodo
    );

    List<Portafolio> findByEstudiante_IdAndAsignaturaAndPeriodo(
            Long estudianteId,
            String asignatura,
            String periodo
    );
}