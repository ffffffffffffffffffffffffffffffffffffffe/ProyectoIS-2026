package com.example.demo.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
@Validated
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Long registrar(
            @NotBlank @Size(max = 100) String nombre,
            @NotBlank @Email @Size(max = 150) String correo,
            @NotBlank
            @Size(min = 8, max = 72, message = "La contraseña debe tener mínimo 8 caracteres") String password) {

        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException(
                    "La contraseña no puede superar 72 bytes en UTF-8."
            );
        }

        String correoNormalizado = correo.strip()
                .toLowerCase(Locale.ROOT);

        if (usuarioRepository.existsByCorreo(correoNormalizado)) {
            throw new IllegalArgumentException(
                    "Ya existe un usuario con ese correo."
            );
        }

        String hash = passwordEncoder.encode(password);

        Usuario usuario = new Usuario(
                nombre.strip(),
                correoNormalizado,
                hash
        );

        return usuarioRepository.save(usuario).getId();
    }

    @Transactional
    public Long registrarConRut(
            @NotBlank @Size(max = 100) String nombre,
            @NotBlank @Email @Size(max = 150) String correo,
            @NotBlank @Size(min = 8, max = 72) String password,
            @NotBlank @Size(max = 12) String rut) {

        String rutNormalizado = RutValidator.normalizarYValidar(rut);

        if (usuarioRepository.existsByRut(rutNormalizado)) {
            throw new IllegalArgumentException(
                    "Ya existe un usuario con ese RUT."
            );
        }

        // Conserva las comprobaciones de correo y contraseña existentes.
        Long id = registrar(nombre, correo, password);

        Usuario usuario = usuarioRepository.findById(id).orElseThrow();
        usuario.setRut(rutNormalizado);

        return usuarioRepository.saveAndFlush(usuario).getId();
    }
}