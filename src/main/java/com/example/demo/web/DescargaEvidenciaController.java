package com.example.demo.web;

import com.example.demo.evidencia.EvidenciaService;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/evidencias")
@PreAuthorize("hasRole('ESTUDIANTE')")
public class DescargaEvidenciaController {

    private final EvidenciaService evidenciaService;

    public DescargaEvidenciaController(
            EvidenciaService evidenciaService) {
        this.evidenciaService = evidenciaService;
    }

    @GetMapping("/{id}/descargar")
    public ResponseEntity<Resource> descargar(
            @PathVariable("id") Long id) {

        var descarga = evidenciaService.descargarPropia(id);

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