package com.uce.servidorproyecto.dto;

import java.util.ArrayList;
import java.util.List;

public class ResultadoReagendamiento {

    private boolean exito;
    private boolean guardado;
    private String error;
    private final List<String> mensajes = new ArrayList<>();
    private int desplazamientosRealizados;

    public boolean isExito() { return exito; }
    public void setExito(boolean exito) { this.exito = exito; }

    public boolean isGuardado() { return guardado; }
    public void setGuardado(boolean guardado) { this.guardado = guardado; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public List<String> getMensajes() { return mensajes; }

    public int getDesplazamientosRealizados() { return desplazamientosRealizados; }
    public void setDesplazamientosRealizados(int desplazamientosRealizados) {
        this.desplazamientosRealizados = desplazamientosRealizados;
    }

    public void agregarMensaje(String mensaje) {
        if (mensaje != null && !mensaje.isBlank()) {
            mensajes.add(mensaje);
        }
    }
}
