package com.uce.servidorproyecto.service;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class AnalyticsChartGenerator {

    private AnalyticsChartGenerator() {}

    public record ChartImage(String titulo, byte[] png) {}

    @SuppressWarnings({"unchecked", "deprecation"})
    public static List<ChartImage> generarTodas(
            Map<String, Object> estres,
            Map<String, Object> actividades,
            Map<String, Object> reagendamientos,
            Map<String, Object> tipo,
            Map<String, Object> dia,
            Map<String, Object> bienestar,
            Map<String, Object> usuarios,
            Map<String, Object> semanas) throws IOException {

        List<ChartImage> out = new ArrayList<>();

        List<Map<String, Object>> seriesEstres = (List<Map<String, Object>>) estres.get("series");
        if (seriesEstres != null && !seriesEstres.isEmpty()) {
            DefaultCategoryDataset ds = new DefaultCategoryDataset();
            int limit = Math.min(seriesEstres.size(), 12);
            for (int i = 0; i < limit; i++) {
                Map<String, Object> s = seriesEstres.get(i);
                ds.addValue(((Number) s.get("estresPromedio")).doubleValue(), "Estrés", truncar(cohorte(s)));
            }
            out.add(render("Estrés promedio por cohorte",
                    ChartFactory.createBarChart("Estrés promedio por cohorte", "", "Nivel", ds,
                            PlotOrientation.VERTICAL, false, true, false)));
        }

        List<Map<String, Object>> seriesAct = (List<Map<String, Object>>) actividades.get("series");
        if (seriesAct != null && !seriesAct.isEmpty()) {
            DefaultCategoryDataset ds = new DefaultCategoryDataset();
            int limit = Math.min(seriesAct.size(), 10);
            for (int i = 0; i < limit; i++) {
                Map<String, Object> s = seriesAct.get(i);
                String label = truncar(cohorte(s));
                ds.addValue(((Number) s.get("creadas")).doubleValue(), "Creadas", label);
                ds.addValue(((Number) s.get("completadas")).doubleValue(), "Completadas", label);
            }
            out.add(render("Actividades creadas vs completadas por cohorte",
                    ChartFactory.createBarChart("Creadas vs Completadas", "", "Cantidad", ds,
                            PlotOrientation.VERTICAL, true, true, false)));
        }

        DefaultPieDataset<String> reagPie = new DefaultPieDataset<>();
        reagPie.setValue("Exitosos", toDouble(reagendamientos.get("exitosos")));
        reagPie.setValue("Fallidos", toDouble(reagendamientos.get("fallidos")));
        out.add(render("Motor de reagendamiento automático",
                ChartFactory.createPieChart("Reagendamientos automáticos", reagPie, true, true, false)));

        List<Map<String, Object>> seriesTipo = (List<Map<String, Object>>) tipo.get("series");
        if (seriesTipo != null && !seriesTipo.isEmpty()) {
            DefaultPieDataset<String> tipoPie = new DefaultPieDataset<>();
            for (Map<String, Object> s : seriesTipo) {
                tipoPie.setValue(String.valueOf(s.get("tipo")), ((Number) s.get("total")).doubleValue());
            }
            out.add(render("Distribución de actividades por tipo",
                    ChartFactory.createPieChart("Actividades por tipo", tipoPie, true, true, false)));
        }

        List<Map<String, Object>> seriesDia = (List<Map<String, Object>>) dia.get("series");
        if (seriesDia != null && !seriesDia.isEmpty()) {
            DefaultCategoryDataset ds = new DefaultCategoryDataset();
            for (Map<String, Object> s : seriesDia) {
                ds.addValue(((Number) s.get("total")).doubleValue(), "Actividades", String.valueOf(s.get("fecha")));
            }
            out.add(render("Actividades programadas (últimos 7 días)",
                    ChartFactory.createLineChart("Actividades por día", "", "Total", ds,
                            PlotOrientation.VERTICAL, false, true, false)));
        }

        List<Map<String, Object>> carga = (List<Map<String, Object>>) bienestar.get("cargaPorCohorte");
        if (carga == null) {
            carga = (List<Map<String, Object>>) bienestar.get("cargaPorCarrera");
        }
        if (carga != null && !carga.isEmpty()) {
            DefaultCategoryDataset ds = new DefaultCategoryDataset();
            int limit = Math.min(carga.size(), 10);
            for (int i = 0; i < limit; i++) {
                Map<String, Object> s = carga.get(i);
                String label = truncar(cohorte(s));
                ds.addValue(((Number) s.get("pomodoros")).doubleValue(), "Pomodoros", label);
                ds.addValue(((Number) s.get("pausas")).doubleValue(), "Pausas", label);
            }
            out.add(render("Pomodoros y pausas por cohorte",
                    ChartFactory.createBarChart("Bienestar por cohorte", "", "Sesiones", ds,
                            PlotOrientation.VERTICAL, true, true, false)));
        }

        DefaultPieDataset<String> userPie = new DefaultPieDataset<>();
        userPie.setValue("Activos", toDouble(usuarios.get("activos")));
        userPie.setValue("Inactivos", toDouble(usuarios.get("inactivos")));
        out.add(render("Estado de usuarios en la plataforma",
                ChartFactory.createPieChart("Usuarios activos vs inactivos", userPie, true, true, false)));

        List<Map<String, Object>> semCrit = (List<Map<String, Object>>) semanas.get("series");
        if (semCrit != null && !semCrit.isEmpty()) {
            DefaultCategoryDataset ds = new DefaultCategoryDataset();
            for (Map<String, Object> s : semCrit) {
                ds.addValue(((Number) s.get("actividades")).doubleValue(), "Carga", String.valueOf(s.get("semana")));
            }
            out.add(render("Semanas con alta carga",
                    ChartFactory.createBarChart("Semanas críticas", "", "Actividades", ds,
                            PlotOrientation.VERTICAL, false, true, false)));
        }

        return out;
    }

    private static ChartImage render(String titulo, JFreeChart chart) throws IOException {
        BufferedImage image = chart.createBufferedImage(720, 400);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return new ChartImage(titulo, out.toByteArray());
    }

    private static double toDouble(Object v) {
        if (v instanceof Number n) return n.doubleValue();
        return 0;
    }

    private static String cohorte(Map<String, Object> item) {
        if (item == null) return "";
        Object value = item.get("cohorte");
        if (value == null) value = item.get("carrera");
        return value == null ? "" : String.valueOf(value);
    }

    private static String truncar(String s) {
        if (s == null) return "";
        return s.length() > 22 ? s.substring(0, 19) + "..." : s;
    }
}
