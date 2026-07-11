package com.uce.servidorproyecto.dto;

import java.time.LocalTime;

public class ConflictoEvento {

    public enum Origen { ACTIVIDAD, HORARIO_CLASE }

    private Origen origen;
    private Long actividadId;
    private Long horarioClaseId;
    private String titulo;
    private String tipo;
    private int peso;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private boolean inamovible;

    public Origen getOrigen() { return origen; }
    public void setOrigen(Origen origen) { this.origen = origen; }

    public Long getActividadId() { return actividadId; }
    public void setActividadId(Long actividadId) { this.actividadId = actividadId; }

    public Long getHorarioClaseId() { return horarioClaseId; }
    public void setHorarioClaseId(Long horarioClaseId) { this.horarioClaseId = horarioClaseId; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public int getPeso() { return peso; }
    public void setPeso(int peso) { this.peso = peso; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public boolean isInamovible() { return inamovible; }
    public void setInamovible(boolean inamovible) { this.inamovible = inamovible; }

    public boolean esActividad() { return origen == Origen.ACTIVIDAD; }
}
