package com.uce.servidorproyecto.service;

import com.uce.servidorproyecto.model.Actividad;
import com.uce.servidorproyecto.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IAService {

    @Autowired
    private IAProviderService iaProvider;

    // ===== OPTIMIZAR HORARIO CON IA (CLAUDE) =====
    public Map<String, Object> optimizarHorario(List<Actividad> actividades, Usuario usuario) {
        Map<String, Object> resultado = new LinkedHashMap<>();

        // Calcular métricas comunes
        int totalActividades = actividades.size();
        int totalMinutos = actividades.stream()
                .filter(a -> a.getDuracionMinutos() != null)
                .mapToInt(Actividad::getDuracionMinutos)
                .sum();
        String horas = totalMinutos / 60 + "h " + totalMinutos % 60 + "min";

        // 1. Construir prompt detallado
        StringBuilder prompt = new StringBuilder(
            "Eres un asistente académico experto en productividad. Organiza estas tareas en un horario óptimo para hoy, considerando prioridad (ALTA > MEDIA > BAJA) y duración. Devuelve SOLO una lista con hora de inicio, título y prioridad.\n\n"
        );
        for (Actividad a : actividades) {
            prompt.append("- ").append(a.getTitulo())
                  .append(" (").append(a.getDuracionMinutos()).append(" min, prioridad ").append(a.getPrioridad()).append(")\n");
        }
        prompt.append("\nFormato de ejemplo:\n8:00 - Proyecto Final (ALTA)\n10:00 - Tarea Redes (MEDIA)");

        try {
            // 2. Llamar a Claude
            String respuesta = iaProvider.consultar(prompt.toString());

            // 3. Guardar respuesta
            resultado.put("usuario", usuario.getNombre());
            resultado.put("fecha", LocalDate.now().toString());
            resultado.put("totalActividades", totalActividades);
            resultado.put("totalMinutos", totalMinutos);
            resultado.put("horas", horas);

            // 4. Parsear respuesta de Claude a lista de mapas
            List<Map<String, String>> plan = parsearRespuestaClaude(respuesta);
            if (plan.isEmpty()) {
                plan = generarPlanFallback(actividades);
            }
            resultado.put("planOptimizado", plan);
            resultado.put("recomendacion", "✅ Plan generado con IA basado en tus prioridades.");

        } catch (Exception e) {
            // Fallback al orden tradicional
            resultado.put("error", "Falló la IA, usando orden tradicional.");
            resultado.put("usuario", usuario.getNombre());
            resultado.put("fecha", LocalDate.now().toString());
            resultado.put("totalActividades", totalActividades);
            resultado.put("totalMinutos", totalMinutos);
            resultado.put("horas", horas);
            resultado.put("recomendacion", "⚠️ Usando ordenamiento tradicional por prioridad.");
            resultado.put("planOptimizado", generarPlanFallback(actividades));
        }
        return resultado;
    }

    // ===== MÉTODOS AUXILIARES =====

    // Método auxiliar para parsear respuesta de Claude
    private List<Map<String, String>> parsearRespuestaClaude(String respuesta) {
        List<Map<String, String>> plan = new ArrayList<>();
        String[] lineas = respuesta.split("\n");
        for (String linea : lineas) {
            if (linea.trim().isEmpty()) continue;
            // Formato esperado: "8:00 - Proyecto Final (ALTA)"
            String[] partes = linea.split(" - ");
            if (partes.length == 2) {
                Map<String, String> item = new LinkedHashMap<>();
                item.put("hora", partes[0].trim());
                String resto = partes[1].trim();
                if (resto.contains("(") && resto.contains(")")) {
                    int idx1 = resto.indexOf("(");
                    int idx2 = resto.indexOf(")");
                    String prioridad = resto.substring(idx1 + 1, idx2);
                    String titulo = resto.substring(0, idx1).trim();
                    item.put("titulo", titulo);
                    item.put("prioridad", prioridad);
                } else {
                    item.put("titulo", resto);
                    item.put("prioridad", "MEDIA");
                }
                plan.add(item);
            }
        }
        return plan;
    }

    // Método auxiliar para generar plan de fallback
    private List<Map<String, String>> generarPlanFallback(List<Actividad> actividades) {
        List<Actividad> ordenadas = actividades.stream()
                .sorted((a, b) -> {
                    int pa = prioridadValor(a.getPrioridad());
                    int pb = prioridadValor(b.getPrioridad());
                    return Integer.compare(pb, pa); // descendente
                })
                .collect(Collectors.toList());

        List<Map<String, String>> plan = new ArrayList<>();
        LocalTime hora = LocalTime.of(8, 0);
        for (Actividad a : ordenadas) {
            Map<String, String> item = new LinkedHashMap<>();
            item.put("hora", hora.toString());
            item.put("titulo", a.getTitulo());
            item.put("prioridad", a.getPrioridad());
            if (a.getDuracionMinutos() != null) {
                item.put("duracion", a.getDuracionMinutos() + " min");
                hora = hora.plusMinutes(a.getDuracionMinutos() + 5);
            }
            plan.add(item);
        }
        return plan;
    }

    private int prioridadValor(String prioridad) {
        if ("ALTA".equals(prioridad)) return 3;
        if ("MEDIA".equals(prioridad)) return 2;
        return 1; // BAJA
    }

    // ===== APLICAR OPTIMIZACIÓN =====
    public void aplicarOptimizacion(List<Actividad> actividades, Map<String, Object> plan) {
        // Reservado: persistir el plan optimizado en actividades si se requiere en el futuro
    }

    // ===== GENERAR RECURSOS DE ESTUDIO =====
    public Map<String, Object> generarRecursosEstudio(String tema, String tipo) {
        Map<String, Object> recursos = new LinkedHashMap<>();
        recursos.put("tema", tema);
        recursos.put("tipo", tipo != null ? tipo : "general");

        String consulta = construirConsultaRecursos(tema, null, tipo);
        List<Map<String, String>> items = recursosBusquedaValidos(consulta);
        recursos.put("recursos", items);
        recursos.put("mensaje", "Recursos de búsqueda para: " + tema);
        return recursos;
    }

    // ===== SUGERIR PAUSA ACTIVA =====
    public Map<String, String> sugerirPausaActiva() {
        Map<String, String> pausas = new LinkedHashMap<>();
        String[] tipos = {"respiración", "estiramientos", "meditación", "ejercicio", "música"};
        String[] descripciones = {
            "🌬️ Respiración profunda: Inhala 4s, mantén 4s, exhala 4s. Repite 4 veces.",
            "🤸 Estiramientos: Gira el cuello, estira brazos y espalda por 5 minutos.",
            "🧘 Meditación: Cierra los ojos, enfócate en tu respiración por 5 minutos.",
            "🏃 Ejercicio: Haz 10 sentadillas o camina 5 minutos.",
            "🎵 Música: Escucha tu canción favorita y relájate."
        };
        int idx = new Random().nextInt(tipos.length);
        pausas.put("tipo", tipos[idx]);
        pausas.put("descripcion", descripciones[idx]);
        pausas.put("duracion", "5 minutos");
        return pausas;
    }

    // ===== DETECTAR BLOQUEO CREATIVO =====
    public Map<String, Object> detectarBloqueo(List<Actividad> actividadesRecientes) {
        Map<String, Object> resultado = new LinkedHashMap<>();

        long completadas = actividadesRecientes.stream()
                .filter(a -> "COMPLETADA".equals(a.getEstado()))
                .count();

        if (completadas < 3) {
            resultado.put("bloqueo", true);
            resultado.put("nivel", "ALTO");
            resultado.put("mensaje", "🧠 Has completado pocas actividades. ¿Necesitas ayuda?");
            resultado.put("sugerencia", "Prueba el modo auxilio de estudio para obtener recursos y motivación.");
            resultado.put("recursos", generarRecursosEstudio("motivacion", "general"));
        } else if (completadas < 6) {
            resultado.put("bloqueo", true);
            resultado.put("nivel", "MEDIO");
            resultado.put("mensaje", "📚 Vas bien, pero podrías avanzar más. ¡Tú puedes!");
            resultado.put("sugerencia", "Revisa tus tareas prioritarias y usa el método Pomodoro.");
        } else {
            resultado.put("bloqueo", false);
            resultado.put("nivel", "BAJO");
            resultado.put("mensaje", "🔥 ¡Excelente ritmo! Sigue así.");
        }
        return resultado;
    }

    // ===== CHAT COMPAÑERO VIRTUAL =====
    public Map<String, Object> chatCompanero(String mensaje, Usuario usuario, List<Map<String, String>> historial) {
        Map<String, Object> resultado = new LinkedHashMap<>();
        String nombre = usuario.getNombre() != null ? usuario.getNombre() : "usuario";
        String contexto = construirContextoChat(historial);

        String prompt = """
            Eres un asistente de productividad personal de Flowday. Responde en español, tono cercano y útil.
            REGLAS:
            - Entre 300 y 700 caracteres (2-4 oraciones claras).
            - Mantén coherencia con la conversación previa; no repitas saludos si ya conversaron.
            - Responde directamente al último mensaje considerando el contexto.
            Usuario: %s.
            %s
            Nuevo mensaje del usuario: %s
            """.formatted(nombre, contexto, mensaje);

        try {
            String respuesta = iaProvider.consultar(prompt);
            resultado.put("ok", true);
            resultado.put("respuesta", ajustarLongitudRespuestaChat(respuesta.trim()));
            resultado.put("ia", true);
        } catch (Exception e) {
            resultado.put("ok", true);
            resultado.put("respuesta", respuestaChatFallback(mensaje, historial));
            resultado.put("ia", false);
            resultado.put("fallback", true);
        }
        return resultado;
    }

    private String respuestaChatFallback(String mensaje, List<Map<String, String>> historial) {
        String lower = mensaje.toLowerCase();
        boolean hayContexto = historial != null && !historial.isEmpty();
        if (lower.contains("estrés") || lower.contains("estres") || lower.contains("ansiedad")) {
            return hayContexto
                    ? "Siguiendo lo que comentabas, prueba una pausa de 5 minutos con respiración 4-4-4. Luego retoma solo una tarea pequeña y concreta; avanzar un poco ya reduce la presión."
                    : "Cuando sientes mucha carga, una pausa breve ayuda más de lo que parece. Respira profundo un minuto, estira hombros y cuello, y elige una sola tarea pequeña para retomar con calma.";
        }
        if (lower.contains("motiv") || lower.contains("cansad")) {
            return hayContexto
                    ? "Entiendo que cuesta seguir. Divide lo que queda en un bloque de 25 minutos (Pomodoro), sin exigirte perfección. Al terminar ese bloque, reconoce el avance aunque sea mínimo."
                    : "Es normal perder impulso a mitad del semestre. Usa un Pomodoro de 25 minutos con una meta muy concreta y descansa 5 minutos después. La constancia en bloques cortos suele funcionar mejor que estudiar horas enteras agotado.";
        }
        if (lower.contains("estudi") || lower.contains("examen")) {
            return hayContexto
                    ? "Retomando tu tema: repasa primero lo más difícil mientras estás fresco, haz un mapa mental de 5 ideas clave y busca un video corto en YouTube que explique el concepto que te bloquea."
                    : "Para estudiar mejor, empieza por lo que más te cuesta, resume en tus propias palabras y practica con 2-3 ejercicios. Si te atoras, un video breve del tema en YouTube puede destrabar la idea principal.";
        }
        return hayContexto
                ? "Te leo. Cuéntame un poco más sobre lo que necesitas ahora — organización, estudio, estrés o planificación — y lo vemos paso a paso."
                : "Hola, soy tu compañero virtual. Puedo ayudarte con organización, técnicas de estudio, manejo del estrés y planificación de tareas. ¿Qué te gustaría trabajar hoy?";
    }

    private String construirContextoChat(List<Map<String, String>> historial) {
        if (historial == null || historial.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder("Conversación reciente:\n");
        for (Map<String, String> msg : historial) {
            String role = msg.getOrDefault("role", "");
            String text = msg.getOrDefault("text", "");
            if (text.isBlank()) continue;
            if ("user".equals(role)) {
                sb.append("Estudiante: ").append(text).append('\n');
            } else if ("assistant".equals(role) || "bot".equals(role)) {
                sb.append("Tú: ").append(text).append('\n');
            }
        }
        sb.append('\n');
        return sb.toString();
    }

    private String ajustarLongitudRespuestaChat(String texto) {
        if (texto == null) return "";
        String limpio = texto.replaceAll("\\s+", " ").trim();
        if (limpio.length() > 700) {
            int corte = limpio.lastIndexOf('.', 700);
            if (corte < 450) corte = limpio.lastIndexOf(' ', 680);
            if (corte < 450) corte = 700;
            limpio = limpio.substring(0, corte).trim() + "…";
        }
        return limpio;
    }

    private String construirConsultaRecursos(String tema, String titulo, String tipo) {
        StringBuilder sb = new StringBuilder();
        if (tema != null && !tema.isBlank()) sb.append(tema.trim());
        if (titulo != null && !titulo.isBlank()) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(titulo.trim());
        }
        if (tipo != null && !tipo.isBlank()) {
            String t = tipo.toLowerCase();
            if (t.contains("examen") || t.contains("evaluacion") || t.contains("prueba")) {
                sb.append(" preparación examen");
            } else if (t.contains("tarea") || t.contains("deber") || t.contains("entrega")) {
                sb.append(" guía tarea universitaria");
            }
        }
        String q = sb.toString().trim();
        return q.isEmpty() ? "productividad personal" : q;
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private List<Map<String, String>> recursosBusquedaValidos(String consulta) {
        String q = consulta == null || consulta.isBlank() ? "estudio universitario" : consulta.trim();
        List<Map<String, String>> items = new ArrayList<>();
        items.add(recurso("🎥 YouTube: " + q, "https://www.youtube.com/results?search_query=" + urlEncode(q + " explicación")));
        items.add(recurso("📚 Khan Academy: " + q, "https://www.khanacademy.org/search?page_search_query=" + urlEncode(q)));
        items.add(recurso("🔬 Google Scholar: " + q, "https://scholar.google.com/scholar?q=" + urlEncode(q)));
        items.add(recurso("📖 Wikipedia: " + q, "https://es.wikipedia.org/wiki/Special:Search?search=" + urlEncode(q)));
        return items;
    }

    private Map<String, String> recurso(String titulo, String url) {
        Map<String, String> item = new LinkedHashMap<>();
        item.put("titulo", titulo);
        item.put("url", url);
        return item;
    }

    private List<Map<String, String>> validarYNormalizarRecursos(List<Map<String, String>> raw, String consulta) {
        List<Map<String, String>> validos = new ArrayList<>();
        Set<String> urlsVistas = new HashSet<>();

        for (Map<String, String> r : raw) {
            String titulo = r.getOrDefault("titulo", "Recurso educativo");
            String url = normalizarUrlRecurso(r.get("url"), consulta, titulo);
            if (urlsVistas.add(url)) {
                validos.add(recurso(titulo, url));
            }
            if (validos.size() >= 4) break;
        }

        if (validos.size() < 4) {
            for (Map<String, String> fallback : recursosBusquedaValidos(consulta)) {
                String url = fallback.get("url");
                if (urlsVistas.add(url)) {
                    validos.add(fallback);
                }
                if (validos.size() >= 4) break;
            }
        }
        return validos;
    }

    private String normalizarUrlRecurso(String url, String consulta, String titulo) {
        if (esUrlValida(url) && !esDominioBloqueado(url.toLowerCase())) {
            if (url.contains("khanacademy.org") && !url.contains("/search")) {
                return "https://www.khanacademy.org/search?page_search_query=" + urlEncode(consulta);
            }
            return url;
        }
        if (titulo != null && titulo.toLowerCase().contains("scholar")) {
            return "https://scholar.google.com/scholar?q=" + urlEncode(consulta);
        }
        if (titulo != null && titulo.toLowerCase().contains("khan")) {
            return "https://www.khanacademy.org/search?page_search_query=" + urlEncode(consulta);
        }
        if (titulo != null && titulo.toLowerCase().contains("wikipedia")) {
            return "https://es.wikipedia.org/wiki/Special:Search?search=" + urlEncode(consulta);
        }
        return "https://www.youtube.com/results?search_query=" + urlEncode(consulta + " explicación");
    }

    private boolean esDominioBloqueado(String urlLower) {
        return urlLower.contains("coursera.org")
                || urlLower.contains("udemy.com")
                || urlLower.contains("edx.org")
                || urlLower.contains("platzi.com")
                || urlLower.contains("skillshare.com")
                || urlLower.contains("linkedin.com/learning")
                || urlLower.contains("futurelearn.com")
                || urlLower.contains("domestika.org");
    }

    private boolean esUrlValida(String url) {
        if (url == null || url.isBlank() || "#".equals(url.trim())) return false;
        if (!url.startsWith("http://") && !url.startsWith("https://")) return false;
        String lower = url.toLowerCase();
        if (lower.contains("list=pl...") || lower.contains("example.com")) return false;
        if (lower.endsWith("/...") || lower.contains("placeholder")) return false;
        if (esDominioBloqueado(lower)) return false;
        return true;
    }

    private List<Map<String, String>> parsearRecursosJson(String respuesta) {
        List<Map<String, String>> items = new ArrayList<>();
        if (respuesta == null || respuesta.isBlank()) return items;

        String json = respuesta.trim();
        int start = json.indexOf('[');
        int end = json.lastIndexOf(']');
        if (start >= 0 && end > start) {
            json = json.substring(start, end + 1);
        }

        try {
            com.fasterxml.jackson.databind.JsonNode arr =
                    new com.fasterxml.jackson.databind.ObjectMapper().readTree(json);
            if (!arr.isArray()) return items;
            for (com.fasterxml.jackson.databind.JsonNode node : arr) {
                String titulo = node.path("titulo").asText("").trim();
                String url = node.path("url").asText("").trim();
                if (!titulo.isEmpty()) {
                    Map<String, String> item = new LinkedHashMap<>();
                    item.put("titulo", titulo);
                    item.put("url", url);
                    items.add(item);
                }
            }
        } catch (Exception ignored) {
            // fallback vacío
        }
        return items;
    }

}