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
import com.example.demo.usuario.UsuarioRepository;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Controller
@RequestMapping("/admin/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    public AdminUsuarioController(
            UsuarioService usuarioService,
            UsuarioRepository usuarioRepository) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
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
            usuarioService.registrarConRut(
                    form.getNombre(),
                    form.getCorreo(),
                    form.getPassword(),
                    form.getRut()
            );
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("registro", exception.getMessage());
            form.setPassword(null);
            return "admin/usuario-nuevo";
        } catch (DataIntegrityViolationException exception) {
            bindingResult.reject(
                    "registro",
                    "No se pudo registrar el usuario. "
                            + "Comprueba que el correo o RUT no estén registrados."
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

    @GetMapping
    @Transactional(readOnly = true)
    public String listarMiembros(Model model) {
        var miembros = usuarioRepository
                .findAll(Sort.by("nombre").ascending().and(Sort.by("id")))
                .stream()
                .map(usuario -> new MiembroFila(
                        usuario.getId(),
                        usuario.getNombre(),
                        usuario.getCorreo(),
                        usuario.getRoles().stream()
                                .map(rol -> rol.getNombre())
                                .sorted()
                                .toList(),
                        usuario.isActivo()
                ))
                .toList();

        model.addAttribute("miembros", miembros);
        return "admin/usuarios";
    }

    public record MiembroFila(
            Long id,
            String nombre,
            String correo,
            List<String> roles,
            boolean activo) {
    }
}
