package com.uce.servidorproyecto.controller;

import com.uce.servidorproyecto.service.AdminAnalyticsService;
import com.uce.servidorproyecto.service.AdminReportExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminReportController {

    @Autowired
    private AdminAnalyticsService adminAnalyticsService;

    @Autowired
    private AdminReportExportService adminReportExportService;

    private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    // ===== APIs JSON para Chart.js =====

    @GetMapping("/api/analytics/resumen")
    @ResponseBody
    public Map<String, Object> resumen(WebRequest request,
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        AdminAuthHelper.requerirAdmin(request);
        return adminAnalyticsService.getResumen(desde, hasta);
    }

    @GetMapping("/api/analytics/estres-por-cohorte")
    @ResponseBody
    public Map<String, Object> estresPorCohorte(WebRequest request,
                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        AdminAuthHelper.requerirAdmin(request);
        return adminAnalyticsService.getEstresPorCarrera(desde, hasta);
    }

    @GetMapping("/api/analytics/estres-por-carrera")
    @ResponseBody
    public Map<String, Object> estresPorCarreraLegacy(WebRequest request,
                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return estresPorCohorte(request, desde, hasta);
    }

    @GetMapping("/api/analytics/actividades-por-cohorte")
    @ResponseBody
    public Map<String, Object> actividadesPorCohorte(WebRequest request,
                                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        AdminAuthHelper.requerirAdmin(request);
        return adminAnalyticsService.getActividadesPorCarrera(desde, hasta);
    }

    @GetMapping("/api/analytics/actividades-por-carrera")
    @ResponseBody
    public Map<String, Object> actividadesPorCarreraLegacy(WebRequest request,
                                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return actividadesPorCohorte(request, desde, hasta);
    }

    @GetMapping("/api/analytics/reagendamientos")
    @ResponseBody
    public Map<String, Object> reagendamientos(WebRequest request,
                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        AdminAuthHelper.requerirAdmin(request);
        return adminAnalyticsService.getReagendamientos(desde, hasta);
    }

    @GetMapping("/api/analytics/bienestar")
    @ResponseBody
    public Map<String, Object> bienestar(WebRequest request,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        AdminAuthHelper.requerirAdmin(request);
        return adminAnalyticsService.getBienestar(desde, hasta);
    }

    @GetMapping("/api/analytics/uso-plataforma")
    @ResponseBody
    public Map<String, Object> usoPlataforma(WebRequest request,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        AdminAuthHelper.requerirAdmin(request);
        return adminAnalyticsService.getUsoPlataforma(desde, hasta);
    }

    @GetMapping("/api/analytics/actividades-por-tipo")
    @ResponseBody
    public Map<String, Object> actividadesPorTipo(WebRequest request,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        AdminAuthHelper.requerirAdmin(request);
        return adminAnalyticsService.getActividadesPorTipo(desde, hasta);
    }

    @GetMapping("/api/analytics/actividades-por-dia")
    @ResponseBody
    public Map<String, Object> actividadesPorDia(WebRequest request,
                                                 @RequestParam(defaultValue = "7") int dias) {
        AdminAuthHelper.requerirAdmin(request);
        return adminAnalyticsService.getActividadesPorDia(dias);
    }

    @GetMapping("/api/analytics/semanas-criticas")
    @ResponseBody
    public Map<String, Object> semanasCriticas(WebRequest request) {
        AdminAuthHelper.requerirAdmin(request);
        return adminAnalyticsService.getSemanasCriticas();
    }

    @GetMapping("/api/analytics/estado-usuarios")
    @ResponseBody
    public Map<String, Object> estadoUsuarios(WebRequest request) {
        AdminAuthHelper.requerirAdmin(request);
        return adminAnalyticsService.getEstadoUsuarios();
    }

    // ===== Exportación corporativa =====

    @GetMapping("/reportes/export/excel")
    public ResponseEntity<byte[]> exportarExcel(WebRequest request,
                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) throws Exception {
        AdminAuthHelper.requerirAdmin(request);
        byte[] data = adminReportExportService.exportarExcel(desde, hasta);
        String nombre = "reporte-flowday-" + LocalDate.now().format(FILE_DATE) + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombre + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @GetMapping("/reportes/export/pdf")
    public ResponseEntity<byte[]> exportarPdf(WebRequest request,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) throws Exception {
        AdminAuthHelper.requerirAdmin(request);
        byte[] data = adminReportExportService.exportarPdf(desde, hasta);
        String nombre = "reporte-flowday-" + LocalDate.now().format(FILE_DATE) + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombre + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }

    @GetMapping("/reportes/export/csv")
    public ResponseEntity<byte[]> exportarCsv(WebRequest request,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) throws Exception {
        AdminAuthHelper.requerirAdmin(request);
        byte[] data = adminReportExportService.exportarCsvZip(desde, hasta);
        String nombre = "datos-flowday-" + LocalDate.now().format(FILE_DATE) + ".zip";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombre + "\"")
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(data);
    }
}
