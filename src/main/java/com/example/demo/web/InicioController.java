package com.example.demo.web;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class InicioController {

    @GetMapping("/")
    public Map<String, String> inicio(Authentication authentication) {
        return Map.of(
                "mensaje", "Sesión iniciada correctamente",
                "correo", authentication.getName()
        );
    }

    @GetMapping("/admin/comprobar")
    public Map<String, String> comprobarAdministrador() {
        return Map.of(
                "mensaje", "Acceso de administrador autorizado"
        );
    }
}