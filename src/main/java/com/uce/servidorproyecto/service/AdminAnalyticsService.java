package com.uce.servidorproyecto.service;

import com.uce.servidorproyecto.model.Actividad;
import com.uce.servidorproyecto.model.ReagendamientoLog;
import com.uce.servidorproyecto.model.Usuario;
import com.uce.servidorproyecto.repository.ActividadRepository;
import com.uce.servidorproyecto.repository.ConexionRepository;
import com.uce.servidorproyecto.repository.ReagendamientoLogRepository;
import com.uce.servidorproyecto.repository.RegistroBienestarRepository;
import com.uce.servidorproyecto.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminAnalyticsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ActividadRepository actividadRepository;

    @Autowired
    private RegistroBienestarRepository registroBienestarRepository;

    @Autowired
    private ConexionRepository conexionRepository;

    @Autowired
    private ReagendamientoLogRepository reagendamientoLogRepository;

    @Autowired
    private EstresService estresService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private AgrupacionMetricasService agrupacionMetricasService;

    public LocalDateTime[] resolverPeriodo(LocalDate desde, LocalDate hasta) {
        LocalDate h = hasta != null ? hasta : LocalDate.now();
        LocalDate d = desde != null ? desde : h.minusDays(30);
        return new LocalDateTime[]{d.atStartOfDay(), h.atTime(23, 59, 59)};
    }

    public Map<String, Object> getResumen(LocalDate desde, LocalDate hasta) {
        LocalDateTime[] periodo = resolverPeriodo(desde, hasta);
        LocalDateTime inicio = periodo[0];
        LocalDateTime fin = periodo[1];

        List<Actividad> actividades = filtrarActividadesPorPeriodo(inicio, fin);
        long completadas = actividades.stream().filter(a -> "COMPLETADA".equals(a.getEstado())).count();
        long totalUsers = usuarioRepository.countUsers();

        long exitosos = reagendamientoLogRepository.countByExitosoTrueAndFechaEjecucionBetween(inicio, fin);
        long fallidos = reagendamientoLogRepository.countByExitosoFalseAndFechaEjecucionBetween(inicio, fin);
        long totalReag = exitosos + fallidos;

        double estresPromedio = calcularEstresPromedioInstitucional();

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("desde", inicio.toLocalDate().toString());
        res.put("hasta", fin.toLocalDate().toString());
        res.put("totalUsers", totalUsers);
        res.put("totalActividades", actividades.size());
        res.put("actividadesCompletadas", completadas);
        res.put("tasaCompletitud", actividades.isEmpty() ? 0 : round2(completadas / (double) actividades.size()));
        res.put("reagendamientosExitosos", exitosos);
        res.put("reagendamientosFallidos", fallidos);
        res.put("tasaExitoReagendamiento", totalReag == 0 ? 0 : round2(exitosos / (double) totalReag));
        res.put("estresPromedioInstitucional", estresPromedio);
        res.put("agrupacionPor", agrupacionMetricasService.etiquetaAgrupacion());
        return res;
    }

    public Map<String, Object> getEstresPorCarrera(LocalDate desde, LocalDate hasta) {
        LocalDateTime[] periodo = resolverPeriodo(desde, hasta);
        Map<String, Map<String, Object>> grupos = new LinkedHashMap<>();

        for (Usuario u : usuarioRepository.findUsuariosActivos()) {
            String clave = agrupacionMetricasService.claveAgrupacion(u);
            Map<String, Object> agg = grupos.computeIfAbsent(clave, k -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("cohorte", k);
                m.put("usuarios", 0);
                m.put("estresTotal", 0);
                m.put("estresMax", 0);
                return m;
            });
            agg.put("usuarios", (Integer) agg.get("usuarios") + 1);
            int nivel = ((Number) estresService.calcularEstres(u, periodo[1].toLocalDate()).get("nivel")).intValue();
            agg.put("estresTotal", (Integer) agg.get("estresTotal") + nivel);
            agg.put("estresMax", Math.max((Integer) agg.get("estresMax"), nivel));
        }

        List<Map<String, Object>> series = grupos.values().stream()
                .peek(m -> {
                    int est = (Integer) m.get("usuarios");
                    int prom = est > 0 ? Math.round((Integer) m.get("estresTotal") / (float) est) : 0;
                    m.put("estresPromedio", prom);
                    int max = (Integer) m.get("estresMax");
                    m.put("nivelAlerta", max >= 70 ? "ALTO" : max >= 40 ? "MEDIO" : "BAJO");
                })
                .sorted((a, b) -> Integer.compare((Integer) b.get("estresPromedio"), (Integer) a.get("estresPromedio")))
                .collect(Collectors.toList());

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("periodo", Map.of("desde", periodo[0].toLocalDate().toString(), "hasta", periodo[1].toLocalDate().toString()));
        out.put("series", series);
        return out;
    }

    public Map<String, Object> getActividadesPorCarrera(LocalDate desde, LocalDate hasta) {
        LocalDateTime[] periodo = resolverPeriodo(desde, hasta);
        Map<String, Map<String, Object>> grupos = new LinkedHashMap<>();

        for (Actividad a : filtrarActividadesPorPeriodo(periodo[0], periodo[1])) {
            if (a.getUsuario() == null) continue;
            String clave = agrupacionMetricasService.claveAgrupacion(a.getUsuario());
            Map<String, Object> agg = grupos.computeIfAbsent(clave, k -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("cohorte", k);
                m.put("creadas", 0);
                m.put("completadas", 0);
                m.put("completadasATiempo", 0);
                return m;
            });
            agg.put("creadas", (Integer) agg.get("creadas") + 1);
            if ("COMPLETADA".equals(a.getEstado())) {
                agg.put("completadas", (Integer) agg.get("completadas") + 1);
                if (a.getFechaEntrega() == null || !a.getFechaInicio().isAfter(a.getFechaEntrega())) {
                    agg.put("completadasATiempo", (Integer) agg.get("completadasATiempo") + 1);
                }
            }
        }

        List<Map<String, Object>> series = grupos.values().stream()
                .peek(m -> {
                    int creadas = (Integer) m.get("creadas");
                    int comp = (Integer) m.get("completadas");
                    int aTiempo = (Integer) m.get("completadasATiempo");
                    m.put("tasaCompletitud", creadas > 0 ? round2(comp / (double) creadas) : 0);
                    m.put("tasaATiempo", comp > 0 ? round2(aTiempo / (double) comp) : 0);
                })
                .sorted((a, b) -> Integer.compare((Integer) b.get("creadas"), (Integer) a.get("creadas")))
                .collect(Collectors.toList());

        return Map.of("periodo", Map.of("desde", periodo[0].toLocalDate().toString(),
                "hasta", periodo[1].toLocalDate().toString()), "series", series);
    }

    public Map<String, Object> getReagendamientos(LocalDate desde, LocalDate hasta) {
        LocalDateTime[] periodo = resolverPeriodo(desde, hasta);
        List<ReagendamientoLog> logs = reagendamientoLogRepository
                .findByFechaEjecucionBetweenOrderByFechaEjecucionDesc(periodo[0], periodo[1]);

        long exitosos = logs.stream().filter(ReagendamientoLog::isExitoso).count();
        long fallidos = logs.size() - exitosos;

        Map<String, Long> porMotivo = logs.stream()
                .filter(ReagendamientoLog::isExitoso)
                .collect(Collectors.groupingBy(
                        l -> l.getConflictoConTipo() != null ? "Conflicto con " + l.getConflictoConTipo() : "Otro",
                        Collectors.counting()));

        Map<String, Map<String, Long>> porCarrera = new LinkedHashMap<>();
        for (ReagendamientoLog log : logs) {
            if (log.getUsuarioAfectado() == null) continue;
            String carrera = agrupacionMetricasService.claveAgrupacion(log.getUsuarioAfectado());
            Map<String, Long> agg = porCarrera.computeIfAbsent(carrera, k -> {
                Map<String, Long> m = new LinkedHashMap<>();
                m.put("total", 0L);
                m.put("exitosos", 0L);
                return m;
            });
            agg.put("total", agg.get("total") + 1);
            if (log.isExitoso()) agg.put("exitosos", agg.get("exitosos") + 1);
        }

        WeekFields wf = WeekFields.ISO;
        Map<String, Map<String, Long>> tendencia = new TreeMap<>();
        for (ReagendamientoLog log : logs) {
            if (log.getFechaEjecucion() == null) continue;
            LocalDate f = log.getFechaEjecucion().toLocalDate();
            String sem = f.get(wf.weekBasedYear()) + "-S" + f.get(wf.weekOfWeekBasedYear());
            Map<String, Long> t = tendencia.computeIfAbsent(sem, k -> {
                Map<String, Long> m = new LinkedHashMap<>();
                m.put("exitosos", 0L);
                m.put("fallidos", 0L);
                return m;
            });
            if (log.isExitoso()) t.put("exitosos", t.get("exitosos") + 1);
            else t.put("fallidos", t.get("fallidos") + 1);
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("total", logs.size());
        out.put("exitosos", exitosos);
        out.put("fallidos", fallidos);
        out.put("tasaExito", logs.isEmpty() ? 0 : round2(exitosos / (double) logs.size()));
        out.put("porMotivo", porMotivo.entrySet().stream()
                .map(e -> Map.of("motivo", e.getKey(), "cantidad", e.getValue()))
                .collect(Collectors.toList()));
        out.put("porCohorte", porCarrera.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("cohorte", e.getKey());
                    m.put("total", e.getValue().get("total"));
                    m.put("exitosos", e.getValue().get("exitosos"));
                    return m;
                }).collect(Collectors.toList()));
        out.put("tendenciaSemanal", tendencia.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("semana", e.getKey());
                    m.put("exitosos", e.getValue().get("exitosos"));
                    m.put("fallidos", e.getValue().get("fallidos"));
                    return m;
                }).collect(Collectors.toList()));
        return out;
    }

    public Map<String, Object> getBienestar(LocalDate desde, LocalDate hasta) {
        LocalDateTime[] periodo = resolverPeriodo(desde, hasta);
        Map<String, Object> base = adminService.getMonitoreoBienestar();
        base.put("periodo", Map.of("desde", periodo[0].toLocalDate().toString(),
                "hasta", periodo[1].toLocalDate().toString()));
        return base;
    }

    public Map<String, Object> getUsoPlataforma(LocalDate desde, LocalDate hasta) {
        LocalDateTime[] periodo = resolverPeriodo(desde, hasta);
        long activos = usuarioRepository.findUsuariosActivos().stream()
                .filter(u -> u.getUltimoAcceso() != null && u.getUltimoAcceso().isAfter(periodo[0]))
                .count();

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("usuariosActivosPeriodo", activos);
        out.put("totalConexiones", conexionRepository.countAceptadas());
        out.put("pomodorosPeriodo", registroBienestarRepository
                .countAllByTipoAndFechaAfter("POMODORO", periodo[0]));
        out.put("pausasPeriodo", registroBienestarRepository.countAllPausasAfter(periodo[0]));
        out.put("totalAnuncios", adminService.getEstadisticasGenerales().get("totalAnuncios"));
        return out;
    }

    public Map<String, Object> getActividadesPorTipo(LocalDate desde, LocalDate hasta) {
        LocalDateTime[] periodo = resolverPeriodo(desde, hasta);
        Map<String, Long> porTipo = filtrarActividadesPorPeriodo(periodo[0], periodo[1]).stream()
                .collect(Collectors.groupingBy(
                        a -> a.getTipo() != null ? a.getTipo() : "OTRO",
                        Collectors.counting()));
        return Map.of("series", porTipo.entrySet().stream()
                .map(e -> Map.of("tipo", e.getKey(), "total", e.getValue()))
                .collect(Collectors.toList()));
    }

    public Map<String, Object> getActividadesPorDia(int dias) {
        Map<String, Long> resultado = new LinkedHashMap<>();
        LocalDate hoy = LocalDate.now();
        for (int i = dias - 1; i >= 0; i--) {
            LocalDate fecha = hoy.minusDays(i);
            long count = actividadRepository.findAll().stream()
                    .filter(a -> fecha.equals(a.getFechaInicio()))
                    .count();
            resultado.put(fecha.toString(), count);
        }
        return Map.of("series", resultado.entrySet().stream()
                .map(e -> Map.of("fecha", e.getKey(), "total", e.getValue()))
                .collect(Collectors.toList()));
    }

    public Map<String, Object> getSemanasCriticas() {
        return Map.of("series", adminService.getMonitoreoBienestar().get("semanasCriticas"));
    }

    public Map<String, Object> getEstadoUsuarios() {
        List<Usuario> todos = usuarioRepository.findAll();
        long activos = todos.stream().filter(u -> "ACTIVO".equals(u.getEstado())).count();
        long inactivos = todos.size() - activos;
        long users = todos.stream().filter(u -> "USER".equals(u.getRol()) || "ESTUDIANTE".equals(u.getRol())).count();
        long admins = todos.stream().filter(u -> "ADMIN".equals(u.getRol())).count();
        return Map.of("activos", activos, "inactivos", inactivos, "users", users, "admins", admins);
    }

    private List<Actividad> filtrarActividadesPorPeriodo(LocalDateTime inicio, LocalDateTime fin) {
        LocalDate d = inicio.toLocalDate();
        LocalDate h = fin.toLocalDate();
        return actividadRepository.findAll().stream()
                .filter(a -> a.getFechaInicio() != null)
                .filter(a -> !a.getFechaInicio().isBefore(d) && !a.getFechaInicio().isAfter(h))
                .collect(Collectors.toList());
    }

    private double calcularEstresPromedioInstitucional() {
        List<Usuario> usuariosActivos = usuarioRepository.findUsuariosActivos();
        if (usuariosActivos.isEmpty()) return 0;
        int total = 0;
        for (Usuario u : usuariosActivos) {
            total += ((Number) estresService.calcularEstres(u).get("nivel")).intValue();
        }
        return Math.round(total / (double) usuariosActivos.size());
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
