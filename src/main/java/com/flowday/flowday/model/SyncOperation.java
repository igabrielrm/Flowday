package com.flowday.flowday.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sync_operations", uniqueConstraints = @UniqueConstraint(
        name = "uk_sync_user_device_operation",
        columnNames = {"usuario_id", "device_id", "operation_id"}))
public class SyncOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "device_id", nullable = false, length = 200)
    private String deviceId;

    @Column(name = "operation_id", nullable = false)
    private UUID operationId;

    @Column(nullable = false, length = 80)
    private String kind;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "response_json", columnDefinition = "TEXT")
    private String responseJson;

    @Column(length = 1000)
    private String error;

    @Column(name = "server_version")
    private Long serverVersion;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public UUID getOperationId() { return operationId; }
    public void setOperationId(UUID operationId) { this.operationId = operationId; }
    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getResponseJson() { return responseJson; }
    public void setResponseJson(String responseJson) { this.responseJson = responseJson; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public Long getServerVersion() { return serverVersion; }
    public void setServerVersion(Long serverVersion) { this.serverVersion = serverVersion; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
