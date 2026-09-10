package com.example.demo.web;

import com.example.demo.asignacion.SeguimientoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;

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

    @GetMapping("/evidencias/{id}/descargar")
    public ResponseEntity<Resource> descargar(
            @PathVariable("id") Long id) {

        var descarga = servicio.descargarEvidencia(id);

        String disposicion = ContentDisposition.attachment()
                .filename(
                        descarga.nombreOriginal(),
                        StandardCharsets.UTF_8
                )
                .build()
                .toString();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicion)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header("X-Content-Type-Options", "nosniff")
                .body(descarga.recurso());
    }
}