package com.flowday.flowday.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/horario")
public class HorarioController {

    @GetMapping
    public String vistaHorario() {
        return "redirect:/app/schedule";
    }
}
