package com.example.demo.usuario;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String correo) {
        String correoNormalizado = correo.strip()
                .toLowerCase(Locale.ROOT);

        Usuario usuario = usuarioRepository
                .findByCorreo(correoNormalizado)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Credenciales incorrectas."
                        )
                );

        var permisos = usuario.getRoles().stream()
                .map(rol -> new SimpleGrantedAuthority(
                        "ROLE_" + rol.getNombre()
                ))
                .toList();

        return User.withUsername(usuario.getCorreo())
                .password(usuario.getPasswordHash())
                .authorities(permisos)
                .disabled(!usuario.isActivo())
                .build();
    }
}