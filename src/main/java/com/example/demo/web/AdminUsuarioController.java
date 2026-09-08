package com.example.demo.web;

import com.example.demo.usuario.CrearUsuarioForm;
import com.example.demo.usuario.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUsuarioController {

    private final UsuarioService usuarioService;

    public AdminUsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("form", new CrearUsuarioForm());
        return "admin/usuario-nuevo";
    }

    @PostMapping
    public String crearUsuario(
            @Valid @ModelAttribute("form") CrearUsuarioForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            form.setPassword(null);
            return "admin/usuario-nuevo";
        }

        try {
            usuarioService.registrar(
                    form.getNombre(),
                    form.getCorreo(),
                    form.getPassword()
            );
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("registro", exception.getMessage());
            form.setPassword(null);
            return "admin/usuario-nuevo";
        } catch (DataIntegrityViolationException exception) {
            bindingResult.reject(
                    "registro",
                    "No se pudo registrar el usuario. "
                            + "Comprueba que el correo no esté registrado."
            );
            form.setPassword(null);
            return "admin/usuario-nuevo";
        }

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Usuario creado. Ahora puedes asignarle un rol."
        );

        return "redirect:/admin/roles";
    }
}
