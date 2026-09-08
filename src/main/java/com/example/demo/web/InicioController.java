package com.example.demo.web;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class InicioController {

    @GetMapping("/")
    public String inicio(Authentication authentication, Model model) {
        model.addAttribute("correo", authentication.getName());

        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(permiso ->
                        permiso.getAuthority().equals("ROLE_ADMIN")
                );

        model.addAttribute("esAdmin", esAdmin);
        return "inicio";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/admin/comprobar")
    @ResponseBody
    public Map<String, String> comprobarAdministrador() {
        return Map.of(
                "mensaje", "Acceso de administrador autorizado"
        );
    }
}