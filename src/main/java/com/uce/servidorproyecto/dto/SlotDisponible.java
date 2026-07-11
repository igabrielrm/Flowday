package com.uce.servidorproyecto.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class SlotDisponible {

    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;

    public SlotDisponible() {}

    public SlotDisponible(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
}
