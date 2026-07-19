package com.flowday.flowday.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "assistant_actions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_assistant_action_user_key", columnNames = {"usuario_id", "idempotency_key"})
})
public class AssistantAction {

    public enum Type { CREATE_ACTIVITY, RESCHEDULE_ACTIVITY }
    public enum Status { PENDING, CONFIRMED, CANCELLED, EXPIRED }

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Type tipo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payloadJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Status estado;

    @Column(nullable = false)
    private Instant expiraEn;

    @Column(name = "idempotency_key", nullable = false, length = 64)
    private String idempotencyKey;

    @Column(nullable = false, updatable = false)
    private Instant creadoEn;

    private Instant resueltoEn;
    private Long actividadResultadoId;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (estado == null) estado = Status.PENDING;
        if (creadoEn == null) creadoEn = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Type getTipo() { return tipo; }
    public void setTipo(Type tipo) { this.tipo = tipo; }
    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
    public Status getEstado() { return estado; }
    public void setEstado(Status estado) { this.estado = estado; }
    public Instant getExpiraEn() { return expiraEn; }
    public void setExpiraEn(Instant expiraEn) { this.expiraEn = expiraEn; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }
    public Instant getResueltoEn() { return resueltoEn; }
    public void setResueltoEn(Instant resueltoEn) { this.resueltoEn = resueltoEn; }
    public Long getActividadResultadoId() { return actividadResultadoId; }
    public void setActividadResultadoId(Long actividadResultadoId) { this.actividadResultadoId = actividadResultadoId; }
}
