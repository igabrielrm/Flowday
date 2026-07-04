package com.uce.servidorproyecto.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "conexiones", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"solicitante_id", "receptor_id"})
})
public class Conexion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "solicitante_id")
    private Usuario solicitante;

    @ManyToOne(optional = false)
    @JoinColumn(name = "receptor_id")
    private Usuario receptor;

    /** PENDIENTE | ACEPTADA | RECHAZADA — en flujo automático queda ACEPTADA */
    @Column(nullable = false)
    private String estado;

    private LocalDateTime fechaSolicitud;

    public Conexion() {
        this.estado = "ACEPTADA";
        this.fechaSolicitud = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getSolicitante() { return solicitante; }
    public void setSolicitante(Usuario solicitante) { this.solicitante = solicitante; }

    public Usuario getReceptor() { return receptor; }
    public void setReceptor(Usuario receptor) { this.receptor = receptor; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }
}
