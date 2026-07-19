package com.flowday.flowday.service;

import com.flowday.flowday.model.Usuario;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Agrupa métricas por cohorte de registro (año-mes).
 */
@Service
public class AgrupacionMetricasService {

    private static final DateTimeFormatter COHORTE_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    public String claveAgrupacion(Usuario usuario) {
        if (usuario == null || usuario.getFechaRegistro() == null) {
            return "Sin cohorte";
        }
        return usuario.getFechaRegistro().format(COHORTE_FMT);
    }

    public String etiquetaAgrupacion() {
        return "cohorte";
    }
}
