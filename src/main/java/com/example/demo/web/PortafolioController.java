package com.example.demo.web;

import com.example.demo.evidencia.EvidenciaService;
import com.example.demo.portafolio.PortafolioService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/portafolios")
@PreAuthorize("hasRole('ESTUDIANTE')")
public class PortafolioController {

    private final PortafolioService servicio;
    private final EvidenciaService evidenciaService;

    public PortafolioController(
            PortafolioService servicio,
            EvidenciaService evidenciaService) {
        this.servicio = servicio;
        this.evidenciaService = evidenciaService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("portafolios", servicio.listarPropios());
        return "portafolio/lista";
    }

    @PostMapping
    public String crear(
            @RequestParam("asignatura") String asignatura,
            @RequestParam("periodo") String periodo,
            RedirectAttributes redirect) {

        try {
            Long id = servicio.crear(asignatura, periodo);

            redirect.addFlashAttribute(
                    "mensaje", "Portafolio creado correctamente."
            );

            return "redirect:/portafolios/" + id;
        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        } catch (DataIntegrityViolationException exception) {
            redirect.addFlashAttribute(
                    "error",
                    "No se pudo crear el portafolio. "
                            + "Comprueba si ya existe para esa asignatura y periodo."
            );
        }

        redirect.addFlashAttribute("asignaturaIngresada", asignatura);
        redirect.addFlashAttribute("periodoIngresado", periodo);

        return "redirect:/portafolios";
    }

    @GetMapping("/{id}")
    public String detalle(
            @PathVariable("id") Long id,
            Model model) {

        model.addAttribute("portafolio", servicio.consultarPropio(id));
        model.addAttribute("evidencias", evidenciaService.listarPropias(id));

        return "portafolio/detalle";
    }
}