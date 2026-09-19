package com.example.demo.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice(basePackages = "com.example.demo.web")
public class CatalogoPracticas {

    @ModelAttribute("asignaturasDisponibles")
    public List<String> asignaturasDisponibles() {
        return List.of(
                "Práctica 1",
                "Práctica 2",
                "Práctica 3",
                "Práctica 4",
                "Práctica 5",
                "Práctica 6"
        );
    }

    @ModelAttribute("periodosDisponibles")
    public List<String> periodosDisponibles() {
        return List.of("2026-1", "2026-2");
    }
}