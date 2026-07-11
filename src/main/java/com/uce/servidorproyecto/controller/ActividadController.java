package com.uce.servidorproyecto.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/actividades")
public class ActividadController {

    @GetMapping
    public String listar() {
        return "redirect:/app/activities";
    }

    @GetMapping("/nueva")
    public String nueva(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        if (fecha != null) {
            return "redirect:/app/activities/new?fecha=" + fecha;
        }
        return "redirect:/app/activities/new";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id) {
        return "redirect:/app/activities/" + id + "/edit";
    }

    @GetMapping({"/eliminar/{id}", "/estado/{id}", "/reagendar/{id}"})
    public String legacyMutations() {
        return "redirect:/app/activities";
    }

    @PostMapping
    public String legacyPost() {
        return "redirect:/app/activities";
    }

    @PostMapping({"/editar/{id}", "/eliminar/{id}", "/estado/{id}", "/reagendar/{id}"})
    public String legacyPostMutations() {
        return "redirect:/app/activities";
    }
}
