package com.example.demo.asignacion;

import com.example.demo.portafolio.Portafolio;
import com.example.demo.portafolio.PortafolioRepository;
import com.example.demo.rol.Rol;
import com.example.demo.rol.RolRepository;
import com.example.demo.usuario.Usuario;
import com.example.demo.usuario.UsuarioRepository;
import com.example.demo.usuario.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.evidencia.AlmacenamientoService;
import com.example.demo.evidencia.Evidencia;
import com.example.demo.evidencia.EvidenciaRepository;
import org.junit.jupiter.api.AfterAll;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@WithMockUser(roles = "ADMIN")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SeguimientoServiceTest {

    @Autowired
    private SeguimientoService seguimientoService;

    @Autowired
    private AsignacionSeguimientoService asignacionService;

    @Autowired
    private PortafolioRepository portafolioRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private AlmacenamientoService almacenamientoService;

    @Autowired
    private EvidenciaRepository evidenciaRepository;

    private static final Path DIRECTORIO_TEMPORAL = crearDirectorioTemporal();

    // Contenido sintético: prueba la transferencia de bytes,
// no la validez de un documento PDF completo.
    private static final byte[] CONTENIDO_PRUEBA =
            "%PDF-1.4\nContenido de prueba\n%%EOF\n"
                    .getBytes(StandardCharsets.US_ASCII);

    @DynamicPropertySource
    static void configurarAlmacenamiento(DynamicPropertyRegistry registry) {
        registry.add(
                "app.evidencias.directorio",
                () -> DIRECTORIO_TEMPORAL.toString()
        );
    }

    private static Path crearDirectorioTemporal() {
        try {
            return Files.createTempDirectory("seguimiento-test-");
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    @AfterAll
    static void limpiarArchivosTemporales() throws IOException {
        try (var rutas = Files.walk(DIRECTORIO_TEMPORAL)) {
            for (Path ruta : rutas.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(ruta);
            }
        }
    }

    @Test
    void tutorAsignadoPuedeListarYConsultarPortafolio() {
        Usuario estudiante = crearUsuario("ESTUDIANTE");
        Usuario tutor = crearUsuario("TUTOR");
        Long portafolioId = crearPortafolio(estudiante);

        asignacionService.asignar(
                estudiante.getId(), tutor.getId(), TipoRelacion.TUTOR,
                "Práctica pedagógica", "2026-2"
        );

        iniciarSesion(tutor, "TUTOR");

        var portafolios = seguimientoService.listarPortafolios();

        assertEquals(1, portafolios.size());
        assertEquals(portafolioId, portafolios.getFirst().id());

        var detalle = seguimientoService.consultar(portafolioId);

        assertEquals(portafolioId, detalle.portafolio().id());
        assertEquals(
                estudiante.getCorreo(),
                detalle.portafolio().correo()
        );
        assertTrue(detalle.evidencias().isEmpty());
    }

    @Test
    void tutorNoPuedeConsultarPortafolioAsignadoAOtroTutor() {
        Usuario estudiante = crearUsuario("ESTUDIANTE");
        Usuario tutorAsignado = crearUsuario("TUTOR");
        Usuario otroTutor = crearUsuario("TUTOR");
        Long portafolioId = crearPortafolio(estudiante);

        asignacionService.asignar(
                estudiante.getId(), tutorAsignado.getId(),
                TipoRelacion.TUTOR,
                "Práctica pedagógica", "2026-2"
        );

        iniciarSesion(otroTutor, "TUTOR");

        comprobarAccesoDenegado(portafolioId);
    }

    @Test
    void desactivarAsignacionRevocaAccesoDelTutor() {
        Usuario estudiante = crearUsuario("ESTUDIANTE");
        Usuario tutor = crearUsuario("TUTOR");
        Long portafolioId = crearPortafolio(estudiante);

        Long asignacionId = asignacionService.asignar(
                estudiante.getId(), tutor.getId(), TipoRelacion.TUTOR,
                "Práctica pedagógica", "2026-2"
        );

        iniciarSesion(tutor, "TUTOR");

        assertDoesNotThrow(() ->
                seguimientoService.consultar(portafolioId)
        );

        // Volvemos al administrador para desactivar la asignación.
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "admin-prueba",
                        null,
                        AuthorityUtils.createAuthorityList("ROLE_ADMIN")
                )
        );

        asignacionService.desactivar(asignacionId);

        iniciarSesion(tutor, "TUTOR");

        comprobarAccesoDenegado(portafolioId);
    }

    @Test
    void asignacionDeOtraAsignaturaNoPermiteConsultar() {
        Usuario estudiante = crearUsuario("ESTUDIANTE");
        Usuario tutor = crearUsuario("TUTOR");
        Long portafolioId = crearPortafolio(estudiante);

        asignacionService.asignar(
                estudiante.getId(), tutor.getId(), TipoRelacion.TUTOR,
                "Otra asignatura", "2026-2"
        );

        iniciarSesion(tutor, "TUTOR");

        comprobarAccesoDenegado(portafolioId);
    }

    @Test
    void asignacionDeOtroPeriodoNoPermiteConsultar() {
        Usuario estudiante = crearUsuario("ESTUDIANTE");
        Usuario tutor = crearUsuario("TUTOR");
        Long portafolioId = crearPortafolio(estudiante);

        asignacionService.asignar(
                estudiante.getId(), tutor.getId(), TipoRelacion.TUTOR,
                "Práctica pedagógica", "2026-1"
        );

        iniciarSesion(tutor, "TUTOR");

        comprobarAccesoDenegado(portafolioId);
    }

    private void comprobarAccesoDenegado(Long portafolioId) {
        assertTrue(seguimientoService.listarPortafolios().isEmpty());

        assertThrows(AccessDeniedException.class, () ->
                seguimientoService.consultar(portafolioId)
        );
    }

    private Long crearPortafolio(Usuario estudiante) {
        Portafolio portafolio = new Portafolio(
                estudiante,
                "Práctica pedagógica",
                "2026-2"
        );

        return portafolioRepository.saveAndFlush(portafolio).getId();
    }

    private Usuario crearUsuario(String nombreRol) {
        Long id = usuarioService.registrar(
                "Usuario de prueba",
                "seguimiento-" + UUID.randomUUID() + "@example.com",
                "PruebaSegura2026!"
        );

        Rol rol = rolRepository.findByNombre(nombreRol)
                .orElseGet(() ->
                        rolRepository.save(new Rol(nombreRol))
                );

        Usuario usuario = usuarioRepository.findById(id).orElseThrow();

        usuario.agregarRol(rol);

        return usuarioRepository.saveAndFlush(usuario);
    }

    private void iniciarSesion(Usuario usuario, String rol) {
        var autenticacion = new UsernamePasswordAuthenticationToken(
                usuario.getCorreo(),
                null,
                AuthorityUtils.createAuthorityList("ROLE_" + rol)
        );

        SecurityContextHolder.getContext()
                .setAuthentication(autenticacion);
    }

    @Test
    void tutorAsignadoPuedeDescargarEvidencia() throws IOException {
        Usuario estudiante = crearUsuario("ESTUDIANTE");
        Usuario tutor = crearUsuario("TUTOR");
        Long portafolioId = crearPortafolio(estudiante);
        Long evidenciaId = crearEvidencia(portafolioId, estudiante);

        asignacionService.asignar(
                estudiante.getId(), tutor.getId(), TipoRelacion.TUTOR,
                "Práctica pedagógica", "2026-2"
        );

        iniciarSesion(tutor, "TUTOR");

        var descarga = seguimientoService.descargarEvidencia(evidenciaId);

        assertEquals("prueba.pdf", descarga.nombreOriginal());

        try (var entrada = descarga.recurso().getInputStream()) {
            assertArrayEquals(CONTENIDO_PRUEBA, entrada.readAllBytes());
        }
    }

    @Test
    void otroTutorNoPuedeDescargarEvidenciaAunqueConozcaSuId() {
        Usuario estudiante = crearUsuario("ESTUDIANTE");
        Usuario tutorAsignado = crearUsuario("TUTOR");
        Usuario otroTutor = crearUsuario("TUTOR");
        Long portafolioId = crearPortafolio(estudiante);
        Long evidenciaId = crearEvidencia(portafolioId, estudiante);

        asignacionService.asignar(
                estudiante.getId(), tutorAsignado.getId(), TipoRelacion.TUTOR,
                "Práctica pedagógica", "2026-2"
        );

        iniciarSesion(otroTutor, "TUTOR");

        assertThrows(AccessDeniedException.class, () ->
                seguimientoService.descargarEvidencia(evidenciaId)
        );
    }

    @Test
    void desactivarAsignacionImpideVolverADescargar() {
        Usuario estudiante = crearUsuario("ESTUDIANTE");
        Usuario tutor = crearUsuario("TUTOR");
        Long portafolioId = crearPortafolio(estudiante);
        Long evidenciaId = crearEvidencia(portafolioId, estudiante);

        Long asignacionId = asignacionService.asignar(
                estudiante.getId(), tutor.getId(), TipoRelacion.TUTOR,
                "Práctica pedagógica", "2026-2"
        );

        iniciarSesion(tutor, "TUTOR");

        assertDoesNotThrow(() ->
                seguimientoService.descargarEvidencia(evidenciaId)
        );

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "admin-prueba",
                        null,
                        AuthorityUtils.createAuthorityList("ROLE_ADMIN")
                )
        );

        asignacionService.desactivar(asignacionId);

        iniciarSesion(tutor, "TUTOR");

        assertThrows(AccessDeniedException.class, () ->
                seguimientoService.descargarEvidencia(evidenciaId)
        );
    }

    private Long crearEvidencia(Long portafolioId, Usuario estudiante) {
        var archivo = new MockMultipartFile(
                "archivo",
                "prueba.pdf",
                "application/pdf",
                CONTENIDO_PRUEBA
        );

        var guardado = almacenamientoService.guardarPdf(archivo);

        var portafolio = portafolioRepository.findById(portafolioId)
                .orElseThrow();

        var evidencia = new Evidencia(
                portafolio,
                estudiante,
                "Evidencia de prueba",
                "Documento para verificar permisos de descarga",
                "Actividad de prueba",
                guardado.nombreOriginal(),
                guardado.clave(),
                guardado.tipoContenido(),
                guardado.tamanoBytes()
        );

        return evidenciaRepository.saveAndFlush(evidencia).getId();
    }

}