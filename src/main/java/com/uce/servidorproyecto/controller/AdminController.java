package com.uce.servidorproyecto.controller;

import com.uce.servidorproyecto.config.AppProperties;
import com.uce.servidorproyecto.service.AdminService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private AppProperties appProperties;

    @GetMapping("/dashboard")
    public String dashboard(WebRequest request) {
        String redirect = AdminAuthHelper.requerirAdminMvc(request, appProperties.getAdmin().getLoginPath());
        if (redirect != null) return redirect;
        return "redirect:/app/admin";
    }

    @GetMapping("/estadisticas")
    @ResponseBody
    public Map<String, Object> getEstadisticas(WebRequest request) {
        AdminAuthHelper.requerirAdmin(request);
        return adminService.getEstadisticasGenerales();
    }

    @GetMapping("/top-usuarios")
    @ResponseBody
    public List<Map<String, Object>> getTopUsuarios(@RequestParam(defaultValue = "5") int limite,
                                                      WebRequest request) {
        AdminAuthHelper.requerirAdmin(request);
        return adminService.getTopUsuarios(limite);
    }

    @GetMapping("/export/usuarios.csv")
    public void exportarUsuariosCsv(HttpServletResponse response, WebRequest request) throws Exception {
        AdminAuthHelper.requerirAdmin(request);
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=productividad-usuarios.csv");
        response.setCharacterEncoding("UTF-8");
        PrintWriter w = response.getWriter();
        w.write('\ufeff');
        w.println("Nombre,Correo,Rol,Cohorte,Total actividades,Completadas");
        for (Map<String, Object> u : adminService.getTopUsuarios(500)) {
            w.printf("\"%s\",\"%s\",\"%s\",\"%s\",%s,%s%n",
                    escCsv(String.valueOf(u.get("nombre"))),
                    escCsv(String.valueOf(u.get("correo"))),
                    escCsv(String.valueOf(u.get("rol"))),
                    escCsv(String.valueOf(u.getOrDefault("cohorte", ""))),
                    u.get("totalActividades"),
                    u.get("completadas"));
        }
        w.flush();
    }

    private String escCsv(String s) {
        return s == null ? "" : s.replace("\"", "\"\"");
    }
}
