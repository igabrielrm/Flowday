package com.uce.servidorproyecto.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column(nullable = false)
    private String contrasena;

    @Column(nullable = false)
    private String rol; // USER o ADMIN

    private String telefono;
    private LocalDate fechaNacimiento;
    private String genero;

    private String nombreEmergencia;
    private String telefonoEmergencia;
    private String relacionEmergencia;

    private String tema;

    private String foto;
    private String mascotaTipo;

    @Column(nullable = false)
    private String estado;

    private LocalDateTime fechaRegistro;
    private LocalDateTime ultimoAcceso;

    public Usuario() {
        this.estado = "ACTIVO";
        this.rol = "USER";
        this.tema = "dark";
        this.fechaRegistro = LocalDateTime.now();
        this.mascotaTipo = "default";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public String getNombreEmergencia() { return nombreEmergencia; }
    public void setNombreEmergencia(String nombreEmergencia) { this.nombreEmergencia = nombreEmergencia; }

    public String getTelefonoEmergencia() { return telefonoEmergencia; }
    public void setTelefonoEmergencia(String telefonoEmergencia) { this.telefonoEmergencia = telefonoEmergencia; }

    public String getRelacionEmergencia() { return relacionEmergencia; }
    public void setRelacionEmergencia(String relacionEmergencia) { this.relacionEmergencia = relacionEmergencia; }

    public String getTema() { return tema; }
    public void setTema(String tema) { this.tema = tema; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    public String getMascotaTipo() { return mascotaTipo; }
    public void setMascotaTipo(String mascotaTipo) { this.mascotaTipo = mascotaTipo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public LocalDateTime getUltimoAcceso() { return ultimoAcceso; }
    public void setUltimoAcceso(LocalDateTime ultimoAcceso) { this.ultimoAcceso = ultimoAcceso; }

    /** Rol legible en UI (mapea ESTUDIANTE legacy → USER). */
    public String getRolDisplay() {
        if ("ESTUDIANTE".equals(rol)) return "USER";
        return rol;
    }
}
