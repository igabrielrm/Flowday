package com.uce.servidorproyecto.controller;

import com.uce.servidorproyecto.model.Actividad;
import com.uce.servidorproyecto.model.Usuario;
import com.uce.servidorproyecto.service.ActividadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/calendario")
public class CalendarioController {

    @Autowired
    private ActividadService actividadService;

    // ===== VISTA DEL CALENDARIO =====
    @GetMapping
    public String calendario(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes,
                             WebRequest request, Model model) {
        Usuario usuario = (Usuario) request.getAttribute("usuarioLogueado", WebRequest.SCOPE_SESSION);
        if (usuario == null) return "redirect:/login";

        if (mes == null) mes = YearMonth.now();

        model.addAttribute("usuario", usuario);
        model.addAttribute("mes", mes);
        model.addAttribute("mesNombre", mes.getMonth().getDisplayName(java.time.format.TextStyle.FULL, new java.util.Locale("es")));
        model.addAttribute("anio", mes.getYear());

        return "calendario";
    }

    // ===== API: OBTENER ACTIVIDADES DEL MES (para el calendario) =====
    @GetMapping("/api/actividades")
    @ResponseBody
    public List<Actividad> getActividadesDelMes(@RequestParam int year,
                                                @RequestParam int month,
                                                WebRequest request) {
        Usuario usuario = (Usuario) request.getAttribute("usuarioLogueado", WebRequest.SCOPE_SESSION);
        if (usuario == null) return List.of();

        LocalDate inicio = LocalDate.of(year, month, 1);
        LocalDate fin = inicio.withDayOfMonth(inicio.lengthOfMonth());

        // Obtener todas las actividades del usuario y filtrar por el mes
        List<Actividad> todas = actividadService.listarPorUsuario(usuario);
        return todas.stream()
                .filter(a -> a.getFechaInicio() != null)
                .filter(a -> !a.getFechaInicio().isBefore(inicio) && !a.getFechaInicio().isAfter(fin))
                .collect(Collectors.toList());
    }
}