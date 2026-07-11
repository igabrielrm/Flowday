package com.uce.servidorproyecto.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes_privados", indexes = {
        @Index(name = "idx_mp_dest_fecha", columnList = "destinatario_id,fecha"),
        @Index(name = "idx_mp_par", columnList = "remitente_id,destinatario_id")
})
public class MensajePrivado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "remitente_id")
    private Usuario remitente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "destinatario_id")
    private Usuario destinatario;

    @Column(nullable = false, length = 2000)
    private String contenido;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false)
    private boolean leida;

    public MensajePrivado() {
        this.fecha = LocalDateTime.now();
        this.leida = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getRemitente() { return remitente; }
    public void setRemitente(Usuario remitente) { this.remitente = remitente; }

    public Usuario getDestinatario() { return destinatario; }
    public void setDestinatario(Usuario destinatario) { this.destinatario = destinatario; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public boolean isLeida() { return leida; }
    public void setLeida(boolean leida) { this.leida = leida; }
}
