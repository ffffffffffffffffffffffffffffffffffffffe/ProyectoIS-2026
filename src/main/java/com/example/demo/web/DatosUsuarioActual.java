package com.example.demo.web;

import com.example.demo.usuario.UsuarioRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = "com.example.demo.web")
public class DatosUsuarioActual {

    private final UsuarioRepository usuarioRepository;

    public DatosUsuarioActual(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @ModelAttribute("nombreUsuarioActual")
    public String nombreUsuarioActual(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        return usuarioRepository.findByCorreo(authentication.getName())
                .map(usuario -> usuario.getNombre())
                .orElse(authentication.getName());
    }
}