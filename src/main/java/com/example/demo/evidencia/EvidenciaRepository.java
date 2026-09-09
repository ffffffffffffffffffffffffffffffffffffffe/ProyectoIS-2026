package com.example.demo.evidencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EvidenciaRepository
        extends JpaRepository<Evidencia, Long> {

    List<Evidencia> findByPortafolio_IdOrderByFechaSubidaDesc(
            Long portafolioId
    );

    Optional<Evidencia> findByIdAndPortafolio_Estudiante_Id(
            Long evidenciaId,
            Long estudianteId
    );
}