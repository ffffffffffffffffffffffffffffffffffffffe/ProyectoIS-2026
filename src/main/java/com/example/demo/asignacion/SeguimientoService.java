package com.example.demo.asignacion;

import com.example.demo.evidencia.EvidenciaRepository;
import com.example.demo.portafolio.PortafolioRepository;
import com.example.demo.usuario.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.evidencia.AlmacenamientoService;
import org.springframework.core.io.Resource;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@PreAuthorize(
        "hasAnyRole('PROFESOR', 'COLABORADOR_EVALUADOR', 'TUTOR')"
)
public class SeguimientoService {

    private final UsuarioRepository usuarioRepository;
    private final AsignacionSeguimientoRepository asignacionRepository;
    private final PortafolioRepository portafolioRepository;
    private final EvidenciaRepository evidenciaRepository;
    private final AccesoSeguimientoService accesoService;
    private final AlmacenamientoService almacenamientoService;

    public SeguimientoService(
            UsuarioRepository usuarioRepository,
            AsignacionSeguimientoRepository asignacionRepository,
            PortafolioRepository portafolioRepository,
            EvidenciaRepository evidenciaRepository,
            AccesoSeguimientoService accesoService,
            AlmacenamientoService almacenamientoService) {
        this.usuarioRepository = usuarioRepository;
        this.asignacionRepository = asignacionRepository;
        this.portafolioRepository = portafolioRepository;
        this.evidenciaRepository = evidenciaRepository;
        this.accesoService = accesoService;
        this.almacenamientoService = almacenamientoService;
    }

    @Transactional(readOnly = true)
    public List<PortafolioSeguimiento> listarPortafolios() {
        String correo = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        var responsable = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() ->
                        new AccessDeniedException("Cuenta no disponible.")
                );

        if (!responsable.isActivo()) {
            throw new AccessDeniedException("La cuenta no está activa.");
        }

        Set<String> roles = responsable.getRoles().stream()
                .map(rol -> rol.getNombre())
                .collect(Collectors.toSet());

        Map<Long, PortafolioSeguimiento> resultado = new LinkedHashMap<>();

        var asignaciones = asignacionRepository
                .findByResponsable_IdAndActivoTrue(responsable.getId());

        for (var asignacion : asignaciones) {
            if (!roles.contains(asignacion.getTipoRelacion().name())) {
                continue;
            }

            var portafolios = portafolioRepository
                    .findByEstudiante_IdAndAsignaturaAndPeriodo(
                            asignacion.getEstudiante().getId(),
                            asignacion.getAsignatura(),
                            asignacion.getPeriodo()
                    );

            for (var portafolio : portafolios) {
                resultado.putIfAbsent(
                        portafolio.getId(),
                        new PortafolioSeguimiento(
                                portafolio.getId(),
                                portafolio.getEstudiante().getNombre(),
                                portafolio.getEstudiante().getCorreo(),
                                portafolio.getAsignatura(),
                                portafolio.getPeriodo()
                        )
                );
            }
        }

        return List.copyOf(resultado.values());
    }

    @Transactional(readOnly = true)
    public DetalleSeguimiento consultar(Long portafolioId) {
        accesoService.verificarAcceso(portafolioId);

        var portafolio = portafolioRepository.findById(portafolioId)
                .orElseThrow(() ->
                        new AccessDeniedException("Portafolio no disponible.")
                );

        var evidencias = evidenciaRepository
                .findByPortafolio_IdOrderByFechaSubidaDesc(portafolioId)
                .stream()
                .map(evidencia -> new EvidenciaSeguimiento(
                        evidencia.getId(),
                        evidencia.getTitulo(),
                        evidencia.getActividad(),
                        evidencia.getNombreOriginal(),
                        evidencia.getFechaSubida()
                ))
                .toList();

        return new DetalleSeguimiento(
                new PortafolioSeguimiento(
                        portafolio.getId(),
                        portafolio.getEstudiante().getNombre(),
                        portafolio.getEstudiante().getCorreo(),
                        portafolio.getAsignatura(),
                        portafolio.getPeriodo()
                ),
                evidencias
        );
    }

    public record PortafolioSeguimiento(
            Long id,
            String estudiante,
            String correo,
            String asignatura,
            String periodo) {
    }

    public record EvidenciaSeguimiento(
            Long id,
            String titulo,
            String actividad,
            String nombreOriginal,
            LocalDateTime fechaSubida) {
    }

    public record DetalleSeguimiento(
            PortafolioSeguimiento portafolio,
            List<EvidenciaSeguimiento> evidencias) {
    }

    @Transactional(readOnly = true)
    public DescargaSeguimiento descargarEvidencia(Long evidenciaId) {
        if (evidenciaId == null || evidenciaId <= 0) {
            throw new AccessDeniedException(
                    "La evidencia no está disponible para tu cuenta."
            );
        }

        var evidencia = evidenciaRepository.findById(evidenciaId)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "La evidencia no está disponible para tu cuenta."
                        )
                );

        accesoService.verificarAcceso(
                evidencia.getPortafolio().getId()
        );

        Resource recurso = almacenamientoService.cargar(
                evidencia.getClaveAlmacenamiento()
        );

        return new DescargaSeguimiento(
                recurso,
                evidencia.getNombreOriginal()
        );
    }

    public record DescargaSeguimiento(
            Resource recurso,
            String nombreOriginal) {
    }
}