package com.example.demo.asignacion;

import com.example.demo.usuario.Usuario;
import com.example.demo.usuario.UsuarioRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@PreAuthorize("hasRole('ADMIN')")
public class AsignacionSeguimientoService {

    private final AsignacionSeguimientoRepository asignacionRepository;
    private final UsuarioRepository usuarioRepository;

    public AsignacionSeguimientoService(
            AsignacionSeguimientoRepository asignacionRepository,
            UsuarioRepository usuarioRepository) {
        this.asignacionRepository = asignacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Long asignar(
            Long estudianteId,
            Long responsableId,
            TipoRelacion tipoRelacion,
            String asignatura,
            String periodo) {

        validarId(estudianteId);
        validarId(responsableId);

        if (estudianteId.equals(responsableId)) {
            throw new IllegalArgumentException(
                    "Una persona no puede asignarse como su propio responsable."
            );
        }

        if (tipoRelacion == null) {
            throw new IllegalArgumentException(
                    "Debes seleccionar el tipo de relación."
            );
        }

        String asignaturaNormalizada =
                validarTexto(asignatura, "La asignatura", 100);

        String periodoNormalizado =
                validarTexto(periodo, "El periodo", 20);

        Usuario estudiante = buscarUsuario(estudianteId);
        Usuario responsable = buscarUsuario(responsableId);

        if (!estudiante.isActivo() || !responsable.isActivo()) {
            throw new IllegalArgumentException(
                    "Ambos usuarios deben estar activos."
            );
        }

        if (!tieneRol(estudiante, "ESTUDIANTE")) {
            throw new IllegalArgumentException(
                    "El usuario seleccionado no tiene el rol ESTUDIANTE."
            );
        }

        if (!tieneRol(responsable, tipoRelacion.name())) {
            throw new IllegalArgumentException(
                    "El responsable no tiene el rol "
                            + tipoRelacion.name() + "."
            );
        }

        var existente = asignacionRepository
                .findByEstudiante_IdAndResponsable_IdAndTipoRelacionAndAsignaturaAndPeriodo(
                        estudianteId,
                        responsableId,
                        tipoRelacion,
                        asignaturaNormalizada,
                        periodoNormalizado
                );

        if (existente.isPresent()) {
            AsignacionSeguimiento asignacion = existente.get();
            asignacion.activar();
            return asignacionRepository.save(asignacion).getId();
        }

        AsignacionSeguimiento nueva = new AsignacionSeguimiento(
                estudiante,
                responsable,
                tipoRelacion,
                asignaturaNormalizada,
                periodoNormalizado
        );

        return asignacionRepository.save(nueva).getId();
    }

    @Transactional
    public void desactivar(Long asignacionId) {
        validarId(asignacionId);

        AsignacionSeguimiento asignacion = asignacionRepository
                .findById(asignacionId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "La asignación no existe."
                        )
                );

        asignacion.desactivar();
        asignacionRepository.save(asignacion);
    }

    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe el usuario con identificador " + id + "."
                        )
                );
    }

    private boolean tieneRol(Usuario usuario, String nombreRol) {
        return usuario.getRoles().stream()
                .anyMatch(rol -> rol.getNombre().equals(nombreRol));
    }

    private void validarId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "El identificador debe ser positivo."
            );
        }
    }

    private String validarTexto(
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
                    campo + " admite hasta " + longitudMaxima + " caracteres."
            );
        }

        return normalizado;
    }
}