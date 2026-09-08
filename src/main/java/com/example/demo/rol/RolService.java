package com.example.demo.rol;

import org.springframework.context.ApplicationEventPublisher;
import com.example.demo.usuario.Usuario;
import com.example.demo.usuario.UsuarioRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@PreAuthorize("hasRole('ADMIN')")
public class RolService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final ApplicationEventPublisher eventPublisher;

    public RolService(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            ApplicationEventPublisher eventPublisher) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void asignarRol(Long usuarioId, NombreRol nombreRol) {
        validarParametros(usuarioId, nombreRol);

        Usuario usuario = buscarUsuario(usuarioId);
        Rol rol = buscarRol(nombreRol);

        boolean yaTieneRol = usuario.getRoles().stream()
                .anyMatch(actual ->
                        actual.getNombre().equals(nombreRol.name())
                );

        if (!yaTieneRol) {
            usuario.agregarRol(rol);
            usuarioRepository.save(usuario);

            eventPublisher.publishEvent(
                    new RolesActualizados(usuario.getCorreo())
            );
        }
    }

    @Transactional
    public void quitarRol(Long usuarioId, NombreRol nombreRol) {
        validarParametros(usuarioId, nombreRol);

        Usuario usuario = buscarUsuario(usuarioId);

        String correoActual = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        if (nombreRol == NombreRol.ADMIN
                && usuario.getCorreo().equalsIgnoreCase(correoActual)) {
            throw new IllegalArgumentException(
                    "No puedes quitarte tu propio rol de administrador."
            );
        }

        boolean eliminado = usuario.getRoles().removeIf(
                rol -> rol.getNombre().equals(nombreRol.name())
        );

        if (eliminado) {
            usuarioRepository.save(usuario);

            eventPublisher.publishEvent(
                    new RolesActualizados(usuario.getCorreo())
            );
        }
    }

    private Usuario buscarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El usuario no existe."
                        )
                );
    }

    private Rol buscarRol(NombreRol nombreRol) {
        return rolRepository.findByNombre(nombreRol.name())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El rol no está registrado en la base de datos."
                        )
                );
    }

    private void validarParametros(Long usuarioId, NombreRol nombreRol) {
        if (usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException(
                    "El identificador del usuario debe ser positivo."
            );
        }

        if (nombreRol == null) {
            throw new IllegalArgumentException(
                    "Debes indicar un rol."
            );
        }
    }
}
