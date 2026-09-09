package com.example.demo.web;

import com.example.demo.evidencia.EvidenciaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/portafolios/{portafolioId}/evidencias")
@PreAuthorize("hasRole('ESTUDIANTE')")
public class EvidenciaController {

    private static final Logger log =
            LoggerFactory.getLogger(EvidenciaController.class);

    private final EvidenciaService servicio;

    public EvidenciaController(EvidenciaService servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public String subir(
            @PathVariable("portafolioId") Long portafolioId,
            @RequestParam("titulo") String titulo,
            @RequestParam(value = "descripcion", required = false)
            String descripcion,
            @RequestParam("actividad") String actividad,
            @RequestParam("archivo") MultipartFile archivo,
            RedirectAttributes redirect) {

        try {
            servicio.subir(
                    portafolioId,
                    titulo,
                    descripcion,
                    actividad,
                    archivo
            );

            redirect.addFlashAttribute(
                    "mensaje", "Evidencia guardada correctamente."
            );
        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        } catch (DataAccessException | IllegalStateException exception) {
            log.error("Error al registrar una evidencia.", exception);

            redirect.addFlashAttribute(
                    "error",
                    "No se pudo completar la carga. "
                            + "Revisa la lista antes de volver a intentarlo."
            );
        }

        return "redirect:/portafolios/" + portafolioId;
    }
}