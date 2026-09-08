package com.example.demo.web;

import com.example.demo.rol.NombreRol;
import com.example.demo.rol.RolService;
import com.example.demo.usuario.UsuarioRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/roles")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRolController {

    private final RolService rolService;
    private final UsuarioRepository usuarioRepository;

    public AdminRolController(
            RolService rolService,
            UsuarioRepository usuarioRepository) {
        this.rolService = rolService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public String mostrarFormulario(Model model) {
        var usuarios = usuarioRepository.findAll().stream()
                .map(usuario -> new UsuarioOpcion(
                        usuario.getId(),
                        usuario.getNombre(),
                        usuario.getCorreo()
                ))
                .toList();

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("roles", NombreRol.values());

        return "admin/roles";
    }

    @PostMapping
    public String modificarRol(
            @RequestParam("usuarioId") Long usuarioId,
            @RequestParam("rol") NombreRol rol,
            @RequestParam("accion") String accion,
            RedirectAttributes redirectAttributes) {

        try {
            switch (accion) {
                case "asignar" -> {
                    rolService.asignarRol(usuarioId, rol);
                    redirectAttributes.addFlashAttribute(
                            "mensaje",
                            "El usuario tiene ahora el rol seleccionado."
                    );
                }
                case "quitar" -> {
                    rolService.quitarRol(usuarioId, rol);
                    redirectAttributes.addFlashAttribute(
                            "mensaje",
                            "El usuario ya no tiene el rol seleccionado."
                    );
                }
                default -> throw new IllegalArgumentException(
                        "La acción seleccionada no es válida."
                );
            }
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    exception.getMessage()
            );
        }

        return "redirect:/admin/roles";
    }

    public record UsuarioOpcion(
            Long id,
            String nombre,
            String correo) {
    }
}
