package com.example.demo.asignacion;

import com.example.demo.rol.Rol;
import com.example.demo.rol.RolRepository;
import com.example.demo.usuario.Usuario;
import com.example.demo.usuario.UsuarioRepository;
import com.example.demo.usuario.UsuarioService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AsignacionSeguimientoServiceTest {

    @Autowired
    private AsignacionSeguimientoService servicio;

    @Autowired
    private AsignacionSeguimientoRepository asignacionRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @WithMockUser(roles = "ADMIN")
    void creaAsignacionYEvitaDuplicados() {
        Long estudianteId = crearUsuario("ESTUDIANTE");
        Long tutorId = crearUsuario("TUTOR");

        Long primera = servicio.asignar(
                estudianteId, tutorId, TipoRelacion.TUTOR,
                "Práctica pedagógica", "2026-2"
        );

        Long segunda = servicio.asignar(
                estudianteId, tutorId, TipoRelacion.TUTOR,
                "Práctica pedagógica", "2026-2"
        );

        entityManager.flush();
        entityManager.clear();

        assertEquals(primera, segunda);

        AsignacionSeguimiento guardada = asignacionRepository
                .findById(primera).orElseThrow();

        assertTrue(guardada.isActivo());
        assertEquals(estudianteId, guardada.getEstudiante().getId());
        assertEquals(tutorId, guardada.getResponsable().getId());

        assertEquals(1, asignacionRepository
                .findByResponsable_IdAndActivoTrue(tutorId).size());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void desactivaYReactivaLaMismaAsignacion() {
        Long estudianteId = crearUsuario("ESTUDIANTE");
        Long profesorId = crearUsuario("PROFESOR");

        Long id = servicio.asignar(
                estudianteId, profesorId, TipoRelacion.PROFESOR,
                "Práctica pedagógica", "2026-2"
        );

        servicio.desactivar(id);
        entityManager.flush();
        entityManager.clear();

        assertFalse(asignacionRepository.findById(id)
                .orElseThrow().isActivo());

        assertTrue(asignacionRepository
                .findByResponsable_IdAndActivoTrue(profesorId).isEmpty());

        Long reactivada = servicio.asignar(
                estudianteId, profesorId, TipoRelacion.PROFESOR,
                "Práctica pedagógica", "2026-2"
        );

        entityManager.flush();
        entityManager.clear();

        assertEquals(id, reactivada);
        assertTrue(asignacionRepository.findById(id)
                .orElseThrow().isActivo());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rechazaResponsableSinRolCorrespondiente() {
        Long estudianteId = crearUsuario("ESTUDIANTE");
        Long profesorId = crearUsuario("PROFESOR");

        assertThrows(IllegalArgumentException.class, () ->
                servicio.asignar(
                        estudianteId, profesorId, TipoRelacion.TUTOR,
                        "Práctica pedagógica", "2026-2"
                )
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rechazaEstudianteSinRolEstudiante() {
        Long profesorId = crearUsuario("PROFESOR");
        Long tutorId = crearUsuario("TUTOR");

        assertThrows(IllegalArgumentException.class, () ->
                servicio.asignar(
                        profesorId, tutorId, TipoRelacion.TUTOR,
                        "Práctica pedagógica", "2026-2"
                )
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rechazaResponsableInactivo() {
        Long estudianteId = crearUsuario("ESTUDIANTE");
        Long tutorId = crearUsuario("TUTOR");

        Usuario tutor = usuarioRepository.findById(tutorId)
                .orElseThrow();
        tutor.setActivo(false);
        usuarioRepository.save(tutor);

        assertThrows(IllegalArgumentException.class, () ->
                servicio.asignar(
                        estudianteId, tutorId, TipoRelacion.TUTOR,
                        "Práctica pedagógica", "2026-2"
                )
        );
    }

    @Test
    @WithMockUser(roles = "ESTUDIANTE")
    void estudianteNoPuedeCrearAsignaciones() {
        Long estudianteId = crearUsuario("ESTUDIANTE");
        Long tutorId = crearUsuario("TUTOR");

        assertThrows(AccessDeniedException.class, () ->
                servicio.asignar(
                        estudianteId, tutorId, TipoRelacion.TUTOR,
                        "Práctica pedagógica", "2026-2"
                )
        );
    }

    private Long crearUsuario(String nombreRol) {
        Long id = usuarioService.registrar(
                "Usuario de prueba",
                "asignacion-" + UUID.randomUUID() + "@example.com",
                "PruebaSegura2026!"
        );

        Rol rol = rolRepository.findByNombre(nombreRol)
                .orElseGet(() ->
                        rolRepository.save(new Rol(nombreRol))
                );

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow();

        usuario.agregarRol(rol);
        usuarioRepository.save(usuario);

        return id;
    }
}