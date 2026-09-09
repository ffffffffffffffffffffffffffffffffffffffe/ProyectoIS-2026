package com.example.demo.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class CargaArchivoExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> archivoDemasiadoGrande(
            MaxUploadSizeExceededException exception) {

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(
                        "La carga supera el tamaño permitido. "
                                + "Vuelve al portafolio y selecciona un PDF de hasta 10 MB."
                );
    }
}