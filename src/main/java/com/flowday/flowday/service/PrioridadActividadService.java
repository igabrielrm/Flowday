package com.flowday.flowday.service;

import com.flowday.flowday.model.Actividad;
import org.springframework.stereotype.Service;

/**
 * Matriz de prioridad estricta por tipo de actividad (peso mayor = más prioritario / inamovible).
 */
@Service
public class PrioridadActividadService {

    public static final int NIVEL_INAMOVIBLE = 1;
    public static final int NIVEL_ALTA = 2;
    public static final int NIVEL_MEDIA = 3;
    public static final int NIVEL_BAJA = 4;

    public static final int PESO_CLASE_HORARIO = 100;
    public static final int PESO_INAMOVIBLE = 100;
    public static final int PESO_ALTA = 75;
    public static final int PESO_MEDIA = 50;
    public static final int PESO_BAJA = 25;

    public int calcularPeso(Actividad actividad) {
        if (actividad == null) return PESO_BAJA;
        return calcularPesoPorTipo(actividad.getTipo(), actividad.isEsAcademico(),
                actividad.getTiempoPomodoro());
    }

    public int calcularPesoPorTipo(String tipo, boolean esAcademico, Integer tiempoPomodoro) {
        if (tipo == null || tipo.isBlank()) {
            return esAcademico ? PESO_MEDIA : PESO_BAJA;
        }
        return switch (tipo) {
            case "CLASE", "EXAMEN", "CITA_MEDICA" -> PESO_INAMOVIBLE;
            case "DEBER", "CITA_LABORAL" -> PESO_ALTA;
            case "REUNION_GRUPAL", "TRABAJO_GRUPO" -> PESO_MEDIA;
            case "OTRO" -> (tiempoPomodoro != null && tiempoPomodoro > 0) || !esAcademico
                    ? PESO_BAJA : PESO_MEDIA;
            default -> esAcademico ? PESO_MEDIA : PESO_BAJA;
        };
    }

    public int nivelDesdePeso(int peso) {
        if (peso >= PESO_INAMOVIBLE) return NIVEL_INAMOVIBLE;
        if (peso >= PESO_ALTA) return NIVEL_ALTA;
        if (peso >= PESO_MEDIA) return NIVEL_MEDIA;
        return NIVEL_BAJA;
    }

    public boolean esInamovible(int peso) {
        return peso >= PESO_INAMOVIBLE;
    }

    public String etiquetaTipo(String tipo) {
        if (tipo == null) return "Actividad";
        return switch (tipo) {
            case "CLASE" -> "Clase";
            case "EXAMEN" -> "Examen";
            case "CITA_MEDICA" -> "Cita médica";
            case "DEBER" -> "Deber";
            case "CITA_LABORAL" -> "Cita laboral";
            case "REUNION_GRUPAL" -> "Reunión grupal";
            case "TRABAJO_GRUPO" -> "Trabajo en equipo";
            case "OTRO" -> "Estudio personal";
            default -> tipo;
        };
    }

    public void aplicarPeso(Actividad actividad) {
        if (actividad != null) {
            actividad.setPesoPrioridad(calcularPeso(actividad));
        }
    }
}
