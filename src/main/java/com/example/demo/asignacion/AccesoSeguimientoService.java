package com.example.demo.asignacion;

import com.example.demo.portafolio.Portafolio;
import com.example.demo.portafolio.PortafolioRepository;
import com.example.demo.usuario.Usuario;
import com.example.demo.usuario.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@PreAuthorize(
        "hasAnyRole('PROFESOR', 'COLABORADOR_EVALUADOR', 'TUTOR')"
)
public class AccesoSeguimientoService {

    private final UsuarioRepository usuarioRepository;
    private final PortafolioRepository portafolioRepository;
    private final AsignacionSeguimientoRepository asignacionRepository;

    public AccesoSeguimientoService(
            UsuarioRepository usuarioRepository,
            PortafolioRepository portafolioRepository,
            AsignacionSeguimientoRepository asignacionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.portafolioRepository = portafolioRepository;
        this.asignacionRepository = asignacionRepository;
    }

    @Transactional(readOnly = true)
    public void verificarAcceso(Long portafolioId) {
        if (portafolioId == null || portafolioId <= 0) {
            throw accesoDenegado();
        }

        String correo = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Usuario responsable = usuarioRepository.findByCorreo(correo)
                .orElseThrow(this::accesoDenegado);

        if (!responsable.isActivo()) {
            throw accesoDenegado();
        }

        Portafolio portafolio = portafolioRepository
                .findById(portafolioId)
                .orElseThrow(this::accesoDenegado);

        var asignaciones = asignacionRepository
                .findByResponsable_IdAndEstudiante_IdAndAsignaturaAndPeriodoAndActivoTrue(
                        responsable.getId(),
                        portafolio.getEstudiante().getId(),
                        portafolio.getAsignatura(),
                        portafolio.getPeriodo()
                );

        boolean autorizado = asignaciones.stream()
                .anyMatch(asignacion ->
                        responsable.getRoles().stream()
                                .anyMatch(rol ->
                                        rol.getNombre().equals(
                                                asignacion.getTipoRelacion().name()
                                        )
                                )
                );

        if (!autorizado) {
            throw accesoDenegado();
        }
    }

    private AccessDeniedException accesoDenegado() {
        return new AccessDeniedException(
                "No tienes autorización para consultar este portafolio."
        );
    }
}