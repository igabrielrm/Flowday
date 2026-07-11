package com.uce.servidorproyecto.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorPageController {

    @GetMapping("/error/access-denied")
    public String accessDenied() {
        return "redirect:/app/access-denied";
    }
}
