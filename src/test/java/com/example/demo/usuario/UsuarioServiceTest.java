package com.example.demo.usuario;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UsuarioServiceTest {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registraUsuarioConPasswordHasheada() {
        String correo = "prueba-" + UUID.randomUUID() + "@example.com";
        String password = "PruebaSegura2026!";

        Long id = usuarioService.registrar(
                "Estudiante de prueba",
                correo,
                password
        );

        // Envía los cambios pendientes a MySQL.
        usuarioRepository.flush();

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow();

        assertEquals(correo, usuario.getCorreo());
        assertTrue(usuario.isActivo());
        assertTrue(usuario.getRoles().isEmpty());

        assertNotEquals(password, usuario.getPasswordHash());
        assertTrue(passwordEncoder.matches(
                password,
                usuario.getPasswordHash()
        ));
    }

    @Test
    void rechazaCorreoDuplicado() {
        String correo = "duplicado-" + UUID.randomUUID() + "@example.com";

        usuarioService.registrar(
                "Primer estudiante",
                correo,
                "PruebaSegura2026!"
        );

        assertThrows(IllegalArgumentException.class, () ->
                usuarioService.registrar(
                        "Segundo estudiante",
                        correo,
                        "OtraClaveSegura2026!"
                )
        );
    }
}