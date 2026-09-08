package com.example.demo.rol;

import com.example.demo.usuario.Usuario;
import com.example.demo.usuario.UsuarioRepository;
import com.example.demo.usuario.UsuarioService;
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
class RolServiceTest {

    @Autowired
    private RolService rolService;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void administradorPuedeAsignarRolSinDuplicarlo() {
        asegurarRol(NombreRol.ESTUDIANTE);
        Long usuarioId = crearUsuario();

        rolService.asignarRol(usuarioId, NombreRol.ESTUDIANTE);
        rolService.asignarRol(usuarioId, NombreRol.ESTUDIANTE);
        usuarioRepository.flush();

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow();

        long cantidad = usuario.getRoles().stream()
                .filter(rol -> rol.getNombre().equals("ESTUDIANTE"))
                .count();

        assertEquals(1L, cantidad);
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void administradorPuedeQuitarRol() {
        asegurarRol(NombreRol.ESTUDIANTE);
        Long usuarioId = crearUsuario();

        rolService.asignarRol(usuarioId, NombreRol.ESTUDIANTE);
        rolService.quitarRol(usuarioId, NombreRol.ESTUDIANTE);
        usuarioRepository.flush();

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow();

        assertTrue(usuario.getRoles().isEmpty());
        assertTrue(rolRepository.findByNombre("ESTUDIANTE").isPresent());
    }

    @Test
    @WithMockUser(roles = "ESTUDIANTE")
    void estudianteNoPuedeAsignarRoles() {
        Long usuarioId = crearUsuario();

        assertThrows(AccessDeniedException.class, () ->
                rolService.asignarRol(usuarioId, NombreRol.ADMIN)
        );
    }

    @Test
    @WithMockUser(roles = "ESTUDIANTE")
    void estudianteNoPuedeQuitarRoles() {
        Long usuarioId = crearUsuario();

        assertThrows(AccessDeniedException.class, () ->
                rolService.quitarRol(usuarioId, NombreRol.ESTUDIANTE)
        );
    }

    private Long crearUsuario() {
        return usuarioService.registrar(
                "Usuario de prueba",
                "roles-" + UUID.randomUUID() + "@example.com",
                "PruebaSegura2026!"
        );
    }

    private void asegurarRol(NombreRol nombreRol) {
        if (rolRepository.findByNombre(nombreRol.name()).isEmpty()) {
            rolRepository.save(new Rol(nombreRol.name()));
        }
    }
}
