package com.example.demo.usuario;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

@Service
@PreAuthorize("hasRole('ADMIN')")
public class RestablecerPasswordService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventos;

    public RestablecerPasswordService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            ApplicationEventPublisher eventos) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventos = eventos;
    }

    @Transactional
    public void restablecer(
            Long usuarioId,
            String nuevaPassword,
            String confirmacion) {

        if (usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException("Usuario no válido.");
        }

        if (nuevaPassword == null
                || nuevaPassword.isBlank()
                || nuevaPassword.length() < 8
                || nuevaPassword.length() > 72) {
            throw new IllegalArgumentException(
                    "La contraseña debe tener entre 8 y 72 caracteres."
            );
        }

        if (nuevaPassword.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException(
                    "La contraseña no puede superar 72 bytes en UTF-8."
            );
        }

        if (!nuevaPassword.equals(confirmacion)) {
            throw new IllegalArgumentException(
                    "Las contraseñas no coinciden."
            );
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() ->
                        new IllegalArgumentException("El usuario no existe.")
                );

        usuario.setPasswordHash(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.saveAndFlush(usuario);

        eventos.publishEvent(
                new PasswordRestablecida(usuario.getCorreo())
        );
    }
}