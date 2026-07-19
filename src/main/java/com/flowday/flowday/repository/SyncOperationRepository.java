package com.flowday.flowday.repository;

import com.flowday.flowday.model.SyncOperation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SyncOperationRepository extends JpaRepository<SyncOperation, Long> {
    Optional<SyncOperation> findByUsuarioIdAndDeviceIdAndOperationId(
            Long usuarioId, String deviceId, UUID operationId);
}
