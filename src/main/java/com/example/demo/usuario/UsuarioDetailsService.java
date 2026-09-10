package com.example.demo.usuario;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String rutIngresado) {
        String rutNormalizado;

        try {
            rutNormalizado = RutValidator.normalizarYValidar(rutIngresado);
        } catch (IllegalArgumentException exception) {
            throw new UsernameNotFoundException(
                    "Credenciales incorrectas."
            );
        }

        Usuario usuario = usuarioRepository.findByRut(rutNormalizado)
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

        // El ingreso utiliza RUT. Los servicios existentes siguen
        // identificando la sesión internamente por correo.
        return User.withUsername(usuario.getCorreo())
                .password(usuario.getPasswordHash())
                .authorities(permisos)
                .disabled(!usuario.isActivo())
                .build();
    }
}