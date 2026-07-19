package com.flowday.flowday.service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class AdminReportExportService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Autowired
    private AdminAnalyticsService adminAnalyticsService;

    public byte[] exportarExcel(LocalDate desde, LocalDate hasta) throws IOException {
        Map<String, Object> resumen = adminAnalyticsService.getResumen(desde, hasta);
        Map<String, Object> estres = adminAnalyticsService.getEstresPorCarrera(desde, hasta);
        Map<String, Object> actividades = adminAnalyticsService.getActividadesPorCarrera(desde, hasta);
        Map<String, Object> reagendamientos = adminAnalyticsService.getReagendamientos(desde, hasta);
        Map<String, Object> tipo = adminAnalyticsService.getActividadesPorTipo(desde, hasta);
        Map<String, Object> dia = adminAnalyticsService.getActividadesPorDia(7);
        Map<String, Object> bienestar = adminAnalyticsService.getBienestar(desde, hasta);
        Map<String, Object> usuarios = adminAnalyticsService.getEstadoUsuarios();
        Map<String, Object> semanas = adminAnalyticsService.getSemanasCriticas();

        List<AnalyticsChartGenerator.ChartImage> graficas = AnalyticsChartGenerator.generarTodas(
                estres, actividades, reagendamientos, tipo, dia, bienestar, usuarios, semanas);

        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            CellStyle headerStyle = crearEstiloEncabezado(wb);
            CellStyle titleStyle = crearEstiloTitulo(wb);

            crearHojaPortada(wb, titleStyle, resumen);
            crearHojaResumen(wb, headerStyle, resumen);
            crearHojaEstres(wb, headerStyle, estres);
            crearHojaActividades(wb, headerStyle, actividades);
            crearHojaReagendamientos(wb, headerStyle, reagendamientos);
            crearHojaGraficas(wb, titleStyle, graficas);

            wb.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportarCsvZip(LocalDate desde, LocalDate hasta) throws IOException {
        Map<String, Object> resumen = adminAnalyticsService.getResumen(desde, hasta);
        Map<String, Object> estres = adminAnalyticsService.getEstresPorCarrera(desde, hasta);
        Map<String, Object> actividades = adminAnalyticsService.getActividadesPorCarrera(desde, hasta);
        Map<String, Object> reagendamientos = adminAnalyticsService.getReagendamientos(desde, hasta);

        ByteArrayOutputStream zipOut = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(zipOut)) {
            agregarCsvZip(zos, "01_resumen_ejecutivo.csv", csvResumen(resumen));
            agregarCsvZip(zos, "02_estres_por_cohorte.csv", csvEstres(estres));
            agregarCsvZip(zos, "03_actividades_por_cohorte.csv", csvActividades(actividades));
            agregarCsvZip(zos, "04_reagendamientos.csv", csvReagendamientos(reagendamientos));
        }
        return zipOut.toByteArray();
    }

    public byte[] exportarPdf(LocalDate desde, LocalDate hasta) throws Exception {
        Map<String, Object> resumen = adminAnalyticsService.getResumen(desde, hasta);
        Map<String, Object> estres = adminAnalyticsService.getEstresPorCarrera(desde, hasta);
        Map<String, Object> actividades = adminAnalyticsService.getActividadesPorCarrera(desde, hasta);
        Map<String, Object> reagendamientos = adminAnalyticsService.getReagendamientos(desde, hasta);
        Map<String, Object> tipo = adminAnalyticsService.getActividadesPorTipo(desde, hasta);
        Map<String, Object> dia = adminAnalyticsService.getActividadesPorDia(7);
        Map<String, Object> bienestar = adminAnalyticsService.getBienestar(desde, hasta);
        Map<String, Object> usuarios = adminAnalyticsService.getEstadoUsuarios();
        Map<String, Object> semanas = adminAnalyticsService.getSemanasCriticas();

        List<AnalyticsChartGenerator.ChartImage> graficas = AnalyticsChartGenerator.generarTodas(
                estres, actividades, reagendamientos, tipo, dia, bienestar, usuarios, semanas);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate(), 36, 36, 48, 48);
        PdfWriter.getInstance(doc, out);
        doc.open();

        agregarMembrete(doc);
        doc.add(new Paragraph("Reporte Ejecutivo — Flowday",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
        doc.add(new Paragraph("Plan your day, own your flow.",
                FontFactory.getFont(FontFactory.HELVETICA, 11)));
        doc.add(new Paragraph("Periodo analizado: " + resumen.get("desde") + " al " + resumen.get("hasta"),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
        doc.add(new Paragraph("Generado: " + LocalDateTime.now().format(FMT),
                FontFactory.getFont(FontFactory.HELVETICA, 9)));
        doc.add(Chunk.NEWLINE);

        doc.add(new Paragraph("Resumen ejecutivo", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13)));
        doc.add(tablaResumenPdf(resumen));
        doc.add(Chunk.NEWLINE);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> seriesEstres = (List<Map<String, Object>>) estres.get("series");
        if (seriesEstres != null && !seriesEstres.isEmpty()) {
            doc.add(new Paragraph("Estrés por cohorte", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13)));
            doc.add(tablaEstresPdf(seriesEstres));
            doc.add(Chunk.NEWLINE);
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> seriesAct = (List<Map<String, Object>>) actividades.get("series");
        if (seriesAct != null && !seriesAct.isEmpty()) {
            doc.add(new Paragraph("Actividades por cohorte", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13)));
            doc.add(tablaActividadesPdf(seriesAct));
            doc.add(Chunk.NEWLINE);
        }

        doc.add(new Paragraph("Motor de reagendamiento automático", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13)));
        doc.add(tablaReagendamientosPdf(reagendamientos));
        doc.add(Chunk.NEWLINE);

        doc.newPage();
        doc.add(new Paragraph("Visualización gráfica del periodo " + resumen.get("desde") + " — " + resumen.get("hasta"),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
        doc.add(new Paragraph("Representación equivalente al panel de analítica administrativo",
                FontFactory.getFont(FontFactory.HELVETICA, 9)));
        doc.add(Chunk.NEWLINE);

        for (AnalyticsChartGenerator.ChartImage grafica : graficas) {
            doc.add(new Paragraph(grafica.titulo(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            Image img = Image.getInstance(grafica.png());
            img.scaleToFit(750, 380);
            img.setAlignment(Image.ALIGN_CENTER);
            doc.add(img);
            doc.add(Chunk.NEWLINE);
        }

        doc.close();
        return out.toByteArray();
    }

    private void crearHojaPortada(XSSFWorkbook wb, CellStyle titleStyle, Map<String, Object> resumen) {
        Sheet sheet = wb.createSheet("Portada");
        Row r0 = sheet.createRow(0);
        Cell c0 = r0.createCell(0);
        c0.setCellValue("FLOWDAY");
        c0.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

        Row r1 = sheet.createRow(1);
        r1.createCell(0).setCellValue("Flowday — Reporte Ejecutivo");

        Row r2 = sheet.createRow(3);
        r2.createCell(0).setCellValue("Periodo: " + resumen.get("desde") + " a " + resumen.get("hasta"));

        Row r3 = sheet.createRow(4);
        r3.createCell(0).setCellValue("Generado: " + LocalDateTime.now().format(FMT));

        sheet.setColumnWidth(0, 12000);
    }

    private void crearHojaResumen(XSSFWorkbook wb, CellStyle headerStyle, Map<String, Object> resumen) {
        Sheet sheet = wb.createSheet("Resumen");
        String[] keys = {"totalUsers", "totalActividades", "actividadesCompletadas",
                "tasaCompletitud", "estresPromedioInstitucional", "reagendamientosExitosos",
                "reagendamientosFallidos", "tasaExitoReagendamiento"};
        String[] labels = {"Usuarios", "Actividades", "Completadas", "Tasa completitud",
                "Estrés promedio", "Reagend. exitosos", "Reagend. fallidos", "Tasa éxito reagend."};

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Indicador");
        header.getCell(0).setCellStyle(headerStyle);
        header.createCell(1).setCellValue("Valor");
        header.getCell(1).setCellStyle(headerStyle);

        for (int i = 0; i < keys.length; i++) {
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(labels[i]);
            Object val = resumen.get(keys[i]);
            if (val instanceof Number) row.createCell(1).setCellValue(((Number) val).doubleValue());
            else row.createCell(1).setCellValue(String.valueOf(val));
        }
        sheet.setColumnWidth(0, 8000);
        sheet.setColumnWidth(1, 5000);
    }

    @SuppressWarnings("unchecked")
    private void crearHojaEstres(XSSFWorkbook wb, CellStyle headerStyle, Map<String, Object> estres) {
        Sheet sheet = wb.createSheet("Estrés por cohorte");
        Row header = sheet.createRow(0);
        String[] cols = {"Cohorte", "Usuarios", "Estrés promedio", "Estrés máximo", "Nivel alerta"};
        for (int i = 0; i < cols.length; i++) {
            Cell c = header.createCell(i);
            c.setCellValue(cols[i]);
            c.setCellStyle(headerStyle);
        }

        List<Map<String, Object>> series = (List<Map<String, Object>>) estres.get("series");
        int rowNum = 1;
        if (series != null) {
            for (Map<String, Object> s : series) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(String.valueOf(s.getOrDefault("cohorte", s.get("carrera"))));
                row.createCell(1).setCellValue(((Number) s.getOrDefault("usuarios", s.get("estudiantes"))).intValue());
                row.createCell(2).setCellValue(((Number) s.get("estresPromedio")).intValue());
                row.createCell(3).setCellValue(((Number) s.get("estresMax")).intValue());
                row.createCell(4).setCellValue(String.valueOf(s.get("nivelAlerta")));
            }
        }
        int lastRow = Math.max(rowNum, 2);
        Row avgRow = sheet.createRow(lastRow + 1);
        avgRow.createCell(0).setCellValue("PROMEDIO INSTITUCIONAL");
        avgRow.createCell(2).setCellFormula("AVERAGE(C2:C" + lastRow + ")");
        for (int i = 0; i < 5; i++) sheet.setColumnWidth(i, 6000);
    }

    @SuppressWarnings("unchecked")
    private void crearHojaActividades(XSSFWorkbook wb, CellStyle headerStyle, Map<String, Object> actividades) {
        Sheet sheet = wb.createSheet("Actividades por cohorte");
        Row header = sheet.createRow(0);
        String[] cols = {"Cohorte", "Creadas", "Completadas", "A tiempo", "Tasa completitud", "Tasa a tiempo"};
        for (int i = 0; i < cols.length; i++) {
            Cell c = header.createCell(i);
            c.setCellValue(cols[i]);
            c.setCellStyle(headerStyle);
        }

        List<Map<String, Object>> series = (List<Map<String, Object>>) actividades.get("series");
        int rowNum = 1;
        if (series != null) {
            for (Map<String, Object> s : series) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(String.valueOf(s.getOrDefault("cohorte", s.get("carrera"))));
                row.createCell(1).setCellValue(((Number) s.get("creadas")).intValue());
                row.createCell(2).setCellValue(((Number) s.get("completadas")).intValue());
                row.createCell(3).setCellValue(((Number) s.get("completadasATiempo")).intValue());
                row.createCell(4).setCellValue(((Number) s.get("tasaCompletitud")).doubleValue());
                row.createCell(5).setCellValue(((Number) s.get("tasaATiempo")).doubleValue());
            }
        }
        int lastRow = Math.max(rowNum, 2);
        Row totalRow = sheet.createRow(lastRow + 1);
        totalRow.createCell(0).setCellValue("TOTALES");
        totalRow.createCell(1).setCellFormula("SUM(B2:B" + lastRow + ")");
        totalRow.createCell(2).setCellFormula("SUM(C2:C" + lastRow + ")");
        for (int i = 0; i < 6; i++) sheet.setColumnWidth(i, 5500);
    }

    @SuppressWarnings("unchecked")
    private void crearHojaReagendamientos(XSSFWorkbook wb, CellStyle headerStyle, Map<String, Object> reag) {
        Sheet sheet = wb.createSheet("Reagendamientos");
        Row h = sheet.createRow(0);
        h.createCell(0).setCellValue("Métrica");
        h.getCell(0).setCellStyle(headerStyle);
        h.createCell(1).setCellValue("Valor");
        h.getCell(1).setCellStyle(headerStyle);

        String[][] datos = {
                {"Total", String.valueOf(reag.get("total"))},
                {"Exitosos", String.valueOf(reag.get("exitosos"))},
                {"Fallidos", String.valueOf(reag.get("fallidos"))},
                {"Tasa de éxito", String.valueOf(reag.get("tasaExito"))}
        };
        for (int i = 0; i < datos.length; i++) {
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(datos[i][0]);
            row.createCell(1).setCellValue(datos[i][1]);
        }

        List<Map<String, Object>> porCohorte = (List<Map<String, Object>>) reag.get("porCohorte");
        if (porCohorte == null) {
            porCohorte = (List<Map<String, Object>>) reag.get("porCarrera");
        }
        int start = 7;
        Row h2 = sheet.createRow(start);
        h2.createCell(0).setCellValue("Cohorte");
        h2.getCell(0).setCellStyle(headerStyle);
        h2.createCell(1).setCellValue("Total");
        h2.getCell(1).setCellStyle(headerStyle);
        h2.createCell(2).setCellValue("Exitosos");
        h2.getCell(2).setCellStyle(headerStyle);

        if (porCohorte != null) {
            int r = start + 1;
            for (Map<String, Object> pc : porCohorte) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(String.valueOf(pc.getOrDefault("cohorte", pc.get("carrera"))));
                row.createCell(1).setCellValue(((Number) pc.get("total")).longValue());
                row.createCell(2).setCellValue(((Number) pc.get("exitosos")).longValue());
            }
        }
        sheet.setColumnWidth(0, 8000);
        sheet.setColumnWidth(1, 4000);
        sheet.setColumnWidth(2, 4000);
    }

    private void crearHojaGraficas(XSSFWorkbook wb, CellStyle titleStyle,
                                   List<AnalyticsChartGenerator.ChartImage> graficas) {
        if (graficas == null || graficas.isEmpty()) return;

        XSSFSheet sheet = wb.createSheet("Gráficas ejecutivas");
        sheet.createRow(0).createCell(0).setCellValue(
                "Gráficas del periodo (equivalente al panel de analítica)");
        int row = 2;

        for (AnalyticsChartGenerator.ChartImage grafica : graficas) {
            Row titleRow = sheet.createRow(row++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(grafica.titulo());
            titleCell.setCellStyle(titleStyle);

            int pictureIdx = wb.addPicture(grafica.png(), Workbook.PICTURE_TYPE_PNG);
            XSSFDrawing drawing = sheet.createDrawingPatriarch();
            XSSFClientAnchor anchor = new XSSFClientAnchor();
            anchor.setCol1(0);
            anchor.setRow1(row);
            anchor.setCol2(10);
            anchor.setRow2(row + 22);
            anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
            drawing.createPicture(anchor, pictureIdx);
            row += 24;
        }
        sheet.setColumnWidth(0, 16000);
    }

    private void agregarCsvZip(ZipOutputStream zos, String nombre, String contenido) throws IOException {
        zos.putNextEntry(new ZipEntry(nombre));
        byte[] bom = "\uFEFF".getBytes(StandardCharsets.UTF_8);
        zos.write(bom);
        zos.write(contenido.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private String csvResumen(Map<String, Object> resumen) {
        StringBuilder sb = new StringBuilder("Indicador,Valor\n");
        sb.append("Periodo desde,").append(resumen.get("desde")).append('\n');
        sb.append("Periodo hasta,").append(resumen.get("hasta")).append('\n');
        sb.append("Usuarios,").append(resumen.getOrDefault("totalUsers", resumen.get("totalEstudiantes"))).append('\n');
        sb.append("Actividades,").append(resumen.get("totalActividades")).append('\n');
        sb.append("Completadas,").append(resumen.get("actividadesCompletadas")).append('\n');
        sb.append("Tasa completitud,").append(resumen.get("tasaCompletitud")).append('\n');
        sb.append("Estrés promedio,").append(resumen.get("estresPromedioInstitucional")).append('\n');
        sb.append("Reagend. exitosos,").append(resumen.get("reagendamientosExitosos")).append('\n');
        sb.append("Reagend. fallidos,").append(resumen.get("reagendamientosFallidos")).append('\n');
        sb.append("Tasa éxito reagend.,").append(resumen.get("tasaExitoReagendamiento")).append('\n');
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String csvEstres(Map<String, Object> estres) {
        StringBuilder sb = new StringBuilder("Cohorte,Usuarios,Estrés promedio,Estrés máximo,Nivel alerta\n");
        List<Map<String, Object>> series = (List<Map<String, Object>>) estres.get("series");
        if (series != null) {
            for (Map<String, Object> s : series) {
                sb.append(escCsv(s.getOrDefault("cohorte", s.get("carrera")))).append(',')
                  .append(s.getOrDefault("usuarios", s.get("estudiantes"))).append(',')
                  .append(s.get("estresPromedio")).append(',')
                  .append(s.get("estresMax")).append(',')
                  .append(escCsv(s.get("nivelAlerta"))).append('\n');
            }
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String csvActividades(Map<String, Object> actividades) {
        StringBuilder sb = new StringBuilder("Cohorte,Creadas,Completadas,A tiempo,Tasa completitud,Tasa a tiempo\n");
        List<Map<String, Object>> series = (List<Map<String, Object>>) actividades.get("series");
        if (series != null) {
            for (Map<String, Object> s : series) {
                sb.append(escCsv(s.getOrDefault("cohorte", s.get("carrera")))).append(',')
                  .append(s.get("creadas")).append(',')
                  .append(s.get("completadas")).append(',')
                  .append(s.get("completadasATiempo")).append(',')
                  .append(s.get("tasaCompletitud")).append(',')
                  .append(s.get("tasaATiempo")).append('\n');
            }
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String csvReagendamientos(Map<String, Object> reag) {
        StringBuilder sb = new StringBuilder("Métrica,Valor\n");
        sb.append("Total,").append(reag.get("total")).append('\n');
        sb.append("Exitosos,").append(reag.get("exitosos")).append('\n');
        sb.append("Fallidos,").append(reag.get("fallidos")).append('\n');
        sb.append("Tasa éxito,").append(reag.get("tasaExito")).append('\n');
        sb.append("\nCohorte,Total,Exitosos\n");
        List<Map<String, Object>> porCohorte = (List<Map<String, Object>>) reag.get("porCohorte");
        if (porCohorte == null) {
            porCohorte = (List<Map<String, Object>>) reag.get("porCarrera");
        }
        if (porCohorte != null) {
            for (Map<String, Object> pc : porCohorte) {
                sb.append(escCsv(pc.getOrDefault("cohorte", pc.get("carrera")))).append(',')
                  .append(pc.get("total")).append(',')
                  .append(pc.get("exitosos")).append('\n');
            }
        }
        return sb.toString();
    }

    private String escCsv(Object v) {
        String s = v == null ? "" : String.valueOf(v);
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private CellStyle crearEstiloEncabezado(XSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle crearEstiloTitulo(XSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        return style;
    }

    private void agregarMembrete(Document doc) throws Exception {
        try {
            ClassPathResource res = new ClassPathResource("static/images/uce-escudo.png");
            if (res.exists()) {
                try (InputStream is = res.getInputStream()) {
                    byte[] bytes = is.readAllBytes();
                    Image logo = Image.getInstance(bytes);
                    logo.scaleToFit(60, 60);
                    logo.setAlignment(Image.ALIGN_LEFT);
                    doc.add(logo);
                }
            }
        } catch (Exception ignored) {
            // Logo opcional
        }
    }

    private PdfPTable tablaResumenPdf(Map<String, Object> resumen) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        agregarFilaPdf(table, "Usuarios", resumen.getOrDefault("totalUsers", resumen.get("totalEstudiantes")));
        agregarFilaPdf(table, "Actividades", resumen.get("totalActividades"));
        agregarFilaPdf(table, "Completadas", resumen.get("actividadesCompletadas"));
        agregarFilaPdf(table, "Tasa completitud", resumen.get("tasaCompletitud"));
        agregarFilaPdf(table, "Estrés promedio institucional", resumen.get("estresPromedioInstitucional"));
        agregarFilaPdf(table, "Reagendamientos exitosos", resumen.get("reagendamientosExitosos"));
        agregarFilaPdf(table, "Tasa éxito reagendamiento", resumen.get("tasaExitoReagendamiento"));
        return table;
    }

    private PdfPTable tablaEstresPdf(List<Map<String, Object>> series) {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        agregarCeldaHeaderPdf(table, "Cohorte");
        agregarCeldaHeaderPdf(table, "Usuarios");
        agregarCeldaHeaderPdf(table, "Estrés prom.");
        agregarCeldaHeaderPdf(table, "Estrés máx.");
        agregarCeldaHeaderPdf(table, "Alerta");
        for (Map<String, Object> s : series) {
            table.addCell(String.valueOf(s.getOrDefault("cohorte", s.get("carrera"))));
            table.addCell(String.valueOf(s.getOrDefault("usuarios", s.get("estudiantes"))));
            table.addCell(String.valueOf(s.get("estresPromedio")));
            table.addCell(String.valueOf(s.get("estresMax")));
            table.addCell(String.valueOf(s.get("nivelAlerta")));
        }
        return table;
    }

    private PdfPTable tablaActividadesPdf(List<Map<String, Object>> series) {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        agregarCeldaHeaderPdf(table, "Cohorte");
        agregarCeldaHeaderPdf(table, "Creadas");
        agregarCeldaHeaderPdf(table, "Completadas");
        agregarCeldaHeaderPdf(table, "A tiempo");
        agregarCeldaHeaderPdf(table, "Tasa compl.");
        for (Map<String, Object> s : series) {
            table.addCell(String.valueOf(s.getOrDefault("cohorte", s.get("carrera"))));
            table.addCell(String.valueOf(s.get("creadas")));
            table.addCell(String.valueOf(s.get("completadas")));
            table.addCell(String.valueOf(s.get("completadasATiempo")));
            table.addCell(String.valueOf(s.get("tasaCompletitud")));
        }
        return table;
    }

    private PdfPTable tablaReagendamientosPdf(Map<String, Object> reag) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(60);
        agregarFilaPdf(table, "Total reagendamientos", reag.get("total"));
        agregarFilaPdf(table, "Exitosos", reag.get("exitosos"));
        agregarFilaPdf(table, "Fallidos", reag.get("fallidos"));
        agregarFilaPdf(table, "Tasa de éxito", reag.get("tasaExito"));
        return table;
    }

    private void agregarFilaPdf(PdfPTable table, String label, Object value) {
        table.addCell(label);
        table.addCell(value != null ? String.valueOf(value) : "—");
    }

    private void agregarCeldaHeaderPdf(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9)));
        cell.setBackgroundColor(new java.awt.Color(30, 58, 138));
        table.addCell(cell);
    }
}
