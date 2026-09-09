package com.example.demo.portafolio;

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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@WithMockUser(roles = "ESTUDIANTE")
class PortafolioServiceTest {

    @Autowired
    private PortafolioService servicio;

    @Autowired
    private PortafolioRepository portafolioRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void creaPortafolioParaElEstudianteAutenticado() {
        Usuario estudiante = crearUsuario("ESTUDIANTE");
        iniciarSesion(estudiante.getCorreo(), "ESTUDIANTE");

        Long id = servicio.crear(
                "Práctica pedagógica",
                "2026-2"
        );

        entityManager.flush();
        entityManager.clear();

        Portafolio guardado = portafolioRepository.findById(id)
                .orElseThrow();

        assertEquals(estudiante.getId(), guardado.getEstudiante().getId());
        assertEquals("Práctica pedagógica", guardado.getAsignatura());
        assertEquals("2026-2", guardado.getPeriodo());
        assertNotNull(guardado.getFechaCreacion());
    }

    @Test
    void rechazaPortafolioDuplicado() {
        Usuario estudiante = crearUsuario("ESTUDIANTE");
        iniciarSesion(estudiante.getCorreo(), "ESTUDIANTE");

        servicio.crear("Práctica pedagógica", "2026-2");

        assertThrows(IllegalArgumentException.class, () ->
                servicio.crear("Práctica pedagógica", "2026-2")
        );
    }

    @Test
    void listaUnicamenteLosPortafoliosPropios() {
        Usuario primero = crearUsuario("ESTUDIANTE");
        Usuario segundo = crearUsuario("ESTUDIANTE");

        iniciarSesion(primero.getCorreo(), "ESTUDIANTE");
        Long portafolioPrimero = servicio.crear(
                "Práctica pedagógica", "2026-2"
        );

        iniciarSesion(segundo.getCorreo(), "ESTUDIANTE");
        servicio.crear("Práctica pedagógica", "2026-2");

        entityManager.flush();
        entityManager.clear();

        iniciarSesion(primero.getCorreo(), "ESTUDIANTE");
        var propios = servicio.listarPropios();

        assertEquals(1, propios.size());
        assertEquals(portafolioPrimero, propios.getFirst().id());
    }

    @Test
    void impideConsultarElPortafolioDeOtroEstudiante() {
        Usuario propietario = crearUsuario("ESTUDIANTE");
        Usuario otro = crearUsuario("ESTUDIANTE");

        iniciarSesion(propietario.getCorreo(), "ESTUDIANTE");
        Long id = servicio.crear("Práctica pedagógica", "2026-2");

        iniciarSesion(otro.getCorreo(), "ESTUDIANTE");

        assertThrows(AccessDeniedException.class, () ->
                servicio.consultarPropio(id)
        );
    }

    @Test
    void permiteConsultarElPortafolioPropio() {
        Usuario estudiante = crearUsuario("ESTUDIANTE");
        iniciarSesion(estudiante.getCorreo(), "ESTUDIANTE");

        Long id = servicio.crear("Práctica pedagógica", "2026-2");

        var resultado = servicio.consultarPropio(id);

        assertEquals(id, resultado.id());
        assertEquals("Práctica pedagógica", resultado.asignatura());
    }

    @Test
    void profesorSinRolEstudianteNoPuedeCrearPortafolio() {
        Usuario profesor = crearUsuario("PROFESOR");
        iniciarSesion(profesor.getCorreo(), "PROFESOR");

        assertThrows(AccessDeniedException.class, () ->
                servicio.crear("Práctica pedagógica", "2026-2")
        );
    }

    private Usuario crearUsuario(String nombreRol) {
        Long id = usuarioService.registrar(
                "Usuario de prueba",
                "portafolio-" + UUID.randomUUID() + "@example.com",
                "PruebaSegura2026!"
        );

        Rol rol = rolRepository.findByNombre(nombreRol)
                .orElseGet(() ->
                        rolRepository.save(new Rol(nombreRol))
                );

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow();

        usuario.agregarRol(rol);
        return usuarioRepository.save(usuario);
    }

    private void iniciarSesion(String correo, String rol) {
        var autenticacion =
                UsernamePasswordAuthenticationToken.authenticated(
                        correo,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + rol))
                );

        var contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(autenticacion);
        SecurityContextHolder.setContext(contexto);
    }
}