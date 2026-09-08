package com.example.demo.config;

import com.example.demo.rol.Rol;
import com.example.demo.rol.RolRepository;
import com.example.demo.usuario.Usuario;
import com.example.demo.usuario.UsuarioRepository;
import com.example.demo.usuario.UsuarioService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Component
@Profile("bootstrap")
public class AdministradorInicial implements ApplicationRunner {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final Environment environment;

    public AdministradorInicial(
            UsuarioService usuarioService,
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            Environment environment) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.environment = environment;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String correo = environment
                .getRequiredProperty("ADMIN_EMAIL")
                .strip()
                .toLowerCase(Locale.ROOT);

        // No modifica contraseñas ni concede roles a usuarios existentes.
        if (usuarioRepository.existsByCorreo(correo)) {
            return;
        }

        String password = environment
                .getRequiredProperty("ADMIN_PASSWORD");

        Rol rolAdmin = rolRepository.findByNombre("ADMIN")
                .orElseGet(() -> rolRepository.save(new Rol("ADMIN")));

        Long id = usuarioService.registrar(
                "Administrador inicial",
                correo,
                password
        );

        Usuario administrador = usuarioRepository.findById(id)
                .orElseThrow();

        administrador.agregarRol(rolAdmin);
        usuarioRepository.save(administrador);
    }
}