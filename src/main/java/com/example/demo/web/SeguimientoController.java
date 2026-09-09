package com.example.demo.web;

import com.example.demo.asignacion.SeguimientoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/seguimiento")
@PreAuthorize(
        "hasAnyRole('PROFESOR', 'COLABORADOR_EVALUADOR', 'TUTOR')"
)
public class SeguimientoController {

    private final SeguimientoService servicio;

    public SeguimientoController(SeguimientoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("portafolios", servicio.listarPortafolios());
        return "seguimiento/lista";
    }

    @GetMapping("/portafolios/{id}")
    public String detalle(
            @PathVariable("id") Long id,
            Model model) {
        model.addAttribute("detalle", servicio.consultar(id));
        return "seguimiento/detalle";
    }
}