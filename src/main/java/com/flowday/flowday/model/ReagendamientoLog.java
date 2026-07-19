package com.flowday.flowday.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "reagendamiento_log")
public class ReagendamientoLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "actividad_id")
    private Actividad actividad;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuarioAfectado;

    private LocalDate fechaAnterior;
    private LocalTime horaAnterior;
    private LocalDate fechaNueva;
    private LocalTime horaNueva;

    @Column(length = 500)
    private String motivo;

    /** Columna legacy en BD; se rellena con el mismo texto del motivo. */
    @Column(name = "mensaje_asistente", length = 500)
    private String mensajeAsistente = "";

    private Long conflictoConId;
    private String conflictoConTipo;

    private boolean exitoso;
    private boolean automatico;

    private LocalDateTime fechaEjecucion;

    public ReagendamientoLog() {
        this.fechaEjecucion = LocalDateTime.now();
        this.automatico = true;
        this.mensajeAsistente = "";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Actividad getActividad() { return actividad; }
    public void setActividad(Actividad actividad) { this.actividad = actividad; }

    public Usuario getUsuarioAfectado() { return usuarioAfectado; }
    public void setUsuarioAfectado(Usuario usuarioAfectado) { this.usuarioAfectado = usuarioAfectado; }

    public LocalDate getFechaAnterior() { return fechaAnterior; }
    public void setFechaAnterior(LocalDate fechaAnterior) { this.fechaAnterior = fechaAnterior; }

    public LocalTime getHoraAnterior() { return horaAnterior; }
    public void setHoraAnterior(LocalTime horaAnterior) { this.horaAnterior = horaAnterior; }

    public LocalDate getFechaNueva() { return fechaNueva; }
    public void setFechaNueva(LocalDate fechaNueva) { this.fechaNueva = fechaNueva; }

    public LocalTime getHoraNueva() { return horaNueva; }
    public void setHoraNueva(LocalTime horaNueva) { this.horaNueva = horaNueva; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) {
        this.motivo = motivo;
        if (this.mensajeAsistente == null || this.mensajeAsistente.isBlank()) {
            this.mensajeAsistente = motivo != null ? motivo : "";
        }
    }

    public String getMensajeAsistente() { return mensajeAsistente; }
    public void setMensajeAsistente(String mensajeAsistente) {
        this.mensajeAsistente = mensajeAsistente != null ? mensajeAsistente : "";
    }

    public Long getConflictoConId() { return conflictoConId; }
    public void setConflictoConId(Long conflictoConId) { this.conflictoConId = conflictoConId; }

    public String getConflictoConTipo() { return conflictoConTipo; }
    public void setConflictoConTipo(String conflictoConTipo) { this.conflictoConTipo = conflictoConTipo; }

    public boolean isExitoso() { return exitoso; }
    public void setExitoso(boolean exitoso) { this.exitoso = exitoso; }

    public boolean isAutomatico() { return automatico; }
    public void setAutomatico(boolean automatico) { this.automatico = automatico; }

    public LocalDateTime getFechaEjecucion() { return fechaEjecucion; }
    public void setFechaEjecucion(LocalDateTime fechaEjecucion) { this.fechaEjecucion = fechaEjecucion; }
}
