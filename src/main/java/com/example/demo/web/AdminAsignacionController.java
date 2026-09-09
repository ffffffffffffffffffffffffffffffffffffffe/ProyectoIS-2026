package com.example.demo.web;

import com.example.demo.asignacion.AsignacionSeguimientoRepository;
import com.example.demo.asignacion.AsignacionSeguimientoService;
import com.example.demo.asignacion.TipoRelacion;
import com.example.demo.usuario.Usuario;
import com.example.demo.usuario.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;

@Controller
@RequestMapping("/admin/asignaciones")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAsignacionController {

    private final AsignacionSeguimientoService servicio;
    private final AsignacionSeguimientoRepository asignacionRepository;
    private final UsuarioRepository usuarioRepository;

    public AdminAsignacionController(
            AsignacionSeguimientoService servicio,
            AsignacionSeguimientoRepository asignacionRepository,
            UsuarioRepository usuarioRepository) {
        this.servicio = servicio;
        this.asignacionRepository = asignacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public String mostrar(Model model) {
        var usuarios = usuarioRepository.findAll();

        var estudiantes = usuarios.stream()
                .filter(Usuario::isActivo)
                .filter(usuario -> tieneRol(usuario, Set.of("ESTUDIANTE")))
                .map(this::opcion)
                .toList();

        var responsables = usuarios.stream()
                .filter(Usuario::isActivo)
                .filter(usuario -> tieneRol(usuario, Set.of(
                        "PROFESOR", "COLABORADOR_EVALUADOR", "TUTOR"
                )))
                .map(this::opcion)
                .toList();

        var asignaciones = asignacionRepository.findAll().stream()
                .map(asignacion -> new AsignacionFila(
                        asignacion.getId(),
                        asignacion.getEstudiante().getCorreo(),
                        asignacion.getResponsable().getCorreo(),
                        asignacion.getTipoRelacion().name(),
                        asignacion.getAsignatura(),
                        asignacion.getPeriodo(),
                        asignacion.isActivo()
                ))
                .toList();

        model.addAttribute("estudiantes", estudiantes);
        model.addAttribute("responsables", responsables);
        model.addAttribute("tipos", TipoRelacion.values());
        model.addAttribute("asignaciones", asignaciones);

        return "admin/asignaciones";
    }

    @PostMapping
    public String asignar(
            @RequestParam("estudianteId") Long estudianteId,
            @RequestParam("responsableId") Long responsableId,
            @RequestParam("tipoRelacion") TipoRelacion tipoRelacion,
            @RequestParam("asignatura") String asignatura,
            @RequestParam("periodo") String periodo,
            RedirectAttributes redirect) {

        try {
            servicio.asignar(
                    estudianteId,
                    responsableId,
                    tipoRelacion,
                    asignatura,
                    periodo
            );

            redirect.addFlashAttribute(
                    "mensaje", "La asignación está registrada y activa."
            );
        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        } catch (DataIntegrityViolationException exception) {
            redirect.addFlashAttribute(
                    "error",
                    "No se pudo guardar la asignación. "
                            + "Actualiza la página y comprueba si ya existe."
            );
        }

        return "redirect:/admin/asignaciones";
    }

    @PostMapping("/{id}/desactivar")
    public String desactivar(
            @PathVariable("id") Long id,
            RedirectAttributes redirect) {

        try {
            servicio.desactivar(id);
            redirect.addFlashAttribute(
                    "mensaje", "Asignación desactivada."
            );
        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }

        return "redirect:/admin/asignaciones";
    }

    private boolean tieneRol(Usuario usuario, Set<String> nombres) {
        return usuario.getRoles().stream()
                .anyMatch(rol -> nombres.contains(rol.getNombre()));
    }

    private UsuarioOpcion opcion(Usuario usuario) {
        return new UsuarioOpcion(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo()
        );
    }

    public record UsuarioOpcion(
            Long id,
            String nombre,
            String correo) {
    }

    public record AsignacionFila(
            Long id,
            String estudiante,
            String responsable,
            String tipo,
            String asignatura,
            String periodo,
            boolean activo) {
    }
}