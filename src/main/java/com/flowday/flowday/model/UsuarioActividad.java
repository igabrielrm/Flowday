package com.flowday.flowday.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuario_actividad", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"actividad_id", "usuario_id"})
})
public class UsuarioActividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "actividad_id")
    private Actividad actividad;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false)
    private boolean esPropietario;

    /** Progreso individual del participante invitado */
    @Column(nullable = false)
    private String estadoProgreso;

    /** Borrado lógico cuando el invitado quita la actividad de su calendario */
    @Column(nullable = false)
    private boolean activo;

    public UsuarioActividad() {
        this.estadoProgreso = "PENDIENTE";
        this.activo = true;
        this.esPropietario = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Actividad getActividad() { return actividad; }
    public void setActividad(Actividad actividad) { this.actividad = actividad; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public boolean isEsPropietario() { return esPropietario; }
    public void setEsPropietario(boolean esPropietario) { this.esPropietario = esPropietario; }

    public String getEstadoProgreso() { return estadoProgreso; }
    public void setEstadoProgreso(String estadoProgreso) { this.estadoProgreso = estadoProgreso; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
