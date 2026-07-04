package com.uce.servidorproyecto.controller;

import com.uce.servidorproyecto.model.Actividad;
import com.uce.servidorproyecto.model.Usuario;
import com.uce.servidorproyecto.repository.ActividadRepository;
import com.uce.servidorproyecto.repository.UsuarioRepository;
import com.uce.servidorproyecto.service.ActividadService;
import com.uce.servidorproyecto.service.BienestarService;
import com.uce.servidorproyecto.service.HorarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    @Autowired
    private ActividadService actividadService;

    @Autowired
    private ActividadRepository actividadRepository;

    @Autowired
    private BienestarService bienestarService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private HorarioService horarioService;

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                            WebRequest request, Model model) {
        Usuario usuario = (Usuario) request.getAttribute("usuarioLogueado", WebRequest.SCOPE_SESSION);
        if (usuario == null) return "redirect:/login";

        if (fecha == null) fecha = LocalDate.now();

        List<Actividad> actividadesDia = actividadService.listarPorFecha(usuario, fecha);

        long completadas = actividadesDia.stream()
                .filter(a -> "COMPLETADA".equals(a.getEstado())).count();

        int minutosDia = actividadesDia.stream()
                .filter(a -> a.getDuracionMinutos() != null)
                .mapToInt(Actividad::getDuracionMinutos)
                .sum();
        double horasDia = Math.round(minutosDia / 6.0) / 10.0;

        boolean saturado = actividadService.diaEstasSaturado(usuario);

        int racha = calcularRacha(usuario);

        List<Map<String, Object>> ranking = new ArrayList<>();
        for (Usuario u : usuarioRepository.findAll()) {
            if (u.getId().equals(usuario.getId())) continue;
            if ("ADMIN".equals(u.getRol())) continue;
            Map<String, Object> entry = new HashMap<>();
            entry.put("nombre", u.getNombre() != null ? u.getNombre() : "Usuario");
            entry.put("racha", calcularRacha(u));
            entry.put("carrera", u.getCarrera() != null ? u.getCarrera() : "Sin carrera");
            ranking.add(entry);
        }
        ranking.sort((a, b) -> ((Integer) b.get("racha")).compareTo((Integer) a.get("racha")));
        ranking = ranking.stream().limit(5).collect(Collectors.toList());

        List<Map<String, Object>> alertasPrioridad = actividadService.obtenerAlertasPrioridad(usuario);

        Map<String, Object> statsBienestar = bienestarService.getEstadisticasBienestar(usuario);
        int sesionesPomodoro = statsBienestar.get("totalPomodoros") != null
                ? ((Number) statsBienestar.get("totalPomodoros")).intValue() : 0;

        model.addAttribute("usuario", usuario);
        model.addAttribute("actividadesHoy", actividadesDia);
        model.addAttribute("fechaVista", fecha.toString());
        model.addAttribute("esHoy", fecha.equals(LocalDate.now()));
        model.addAttribute("tareasHoy", actividadesDia.size());
        model.addAttribute("tareasCompletadas", completadas);
        model.addAttribute("horasHoy", horasDia);
        model.addAttribute("saturado", saturado);
        model.addAttribute("alertasPrioridad", alertasPrioridad);
        model.addAttribute("racha", racha);
        model.addAttribute("ranking", ranking);
        model.addAttribute("sesionesPomodoro", sesionesPomodoro);

        horarioService.obtenerAlertaClase(usuario, 15).ifPresent(alerta ->
                model.addAttribute("alertaHorario", alerta));

        return "dashboard";
    }

    private int calcularRacha(Usuario usuario) {
        try {
            List<Actividad> completadas = actividadRepository.findByUsuario(usuario).stream()
                    .filter(a -> "COMPLETADA".equals(a.getEstado()))
                    .collect(Collectors.toList());
            return Math.min(completadas.size(), 30);
        } catch (Exception e) {
            return 0;
        }
    }

    @GetMapping("/css/app.css")
    @ResponseBody
    public Resource getCss() {
        return new ClassPathResource("templates/app.css");
    }
}
