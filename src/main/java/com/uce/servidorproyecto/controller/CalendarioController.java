package com.uce.servidorproyecto.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/calendario")
public class CalendarioController {

    @GetMapping
    public String calendario() {
        return "redirect:/app/calendar";
    }
}
