package com.example.demo.portafolio;

import com.example.demo.usuario.Usuario;
import com.example.demo.usuario.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@PreAuthorize("hasRole('ESTUDIANTE')")
public class PortafolioService {

    private final PortafolioRepository portafolioRepository;
    private final UsuarioRepository usuarioRepository;

    public PortafolioService(
            PortafolioRepository portafolioRepository,
            UsuarioRepository usuarioRepository) {
        this.portafolioRepository = portafolioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Long crear(String asignatura, String periodo) {
        Usuario estudiante = obtenerEstudianteActual();

        String asignaturaNormalizada =
                normalizar(asignatura, "La asignatura", 100);

        String periodoNormalizado =
                normalizar(periodo, "El periodo", 20);

        boolean existe = portafolioRepository
                .existsByEstudiante_IdAndAsignaturaAndPeriodo(
                        estudiante.getId(),
                        asignaturaNormalizada,
                        periodoNormalizado
                );

        if (existe) {
            throw new IllegalArgumentException(
                    "Ya tienes un portafolio para esa asignatura y periodo."
            );
        }

        Portafolio portafolio = new Portafolio(
                estudiante,
                asignaturaNormalizada,
                periodoNormalizado
        );

        return portafolioRepository.save(portafolio).getId();
    }

    @Transactional(readOnly = true)
    public List<PortafolioResumen> listarPropios() {
        Usuario estudiante = obtenerEstudianteActual();

        return portafolioRepository
                .findByEstudiante_IdOrderByFechaCreacionDesc(
                        estudiante.getId()
                )
                .stream()
                .map(this::resumen)
                .toList();
    }

    @Transactional(readOnly = true)
    public PortafolioResumen consultarPropio(Long portafolioId) {
        Usuario estudiante = obtenerEstudianteActual();

        if (portafolioId == null || portafolioId <= 0) {
            throw new IllegalArgumentException(
                    "El identificador del portafolio no es válido."
            );
        }

        Portafolio portafolio = portafolioRepository
                .findByIdAndEstudiante_Id(
                        portafolioId,
                        estudiante.getId()
                )
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "El portafolio no está disponible para tu cuenta."
                        )
                );

        return resumen(portafolio);
    }

    private Usuario obtenerEstudianteActual() {
        String correo = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "No se encontró una cuenta válida."
                        )
                );

        boolean esEstudiante = usuario.getRoles().stream()
                .anyMatch(rol ->
                        rol.getNombre().equals("ESTUDIANTE")
                );

        if (!usuario.isActivo() || !esEstudiante) {
            throw new AccessDeniedException(
                    "Necesitas una cuenta activa con rol ESTUDIANTE."
            );
        }

        return usuario;
    }

    private String normalizar(
            String valor,
            String campo,
            int longitudMaxima) {

        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                    campo + " es obligatorio."
            );
        }

        String normalizado = valor.strip().replaceAll("\\s+", " ");

        if (normalizado.length() > longitudMaxima) {
            throw new IllegalArgumentException(
                    campo + " admite hasta "
                            + longitudMaxima + " caracteres."
            );
        }

        return normalizado;
    }

    private PortafolioResumen resumen(Portafolio portafolio) {
        return new PortafolioResumen(
                portafolio.getId(),
                portafolio.getEstudiante().getNombre(),
                portafolio.getAsignatura(),
                portafolio.getPeriodo(),
                portafolio.getFechaCreacion()
        );
    }

    public record PortafolioResumen(
            Long id,
            String estudiante,
            String asignatura,
            String periodo,
            LocalDateTime fechaCreacion) {
    }
}