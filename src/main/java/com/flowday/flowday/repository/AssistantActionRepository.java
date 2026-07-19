package com.flowday.flowday.repository;

import com.flowday.flowday.model.AssistantAction;
import com.flowday.flowday.model.Usuario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AssistantActionRepository extends JpaRepository<AssistantAction, UUID> {

    Optional<AssistantAction> findByUsuarioAndIdempotencyKey(Usuario usuario, String idempotencyKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AssistantAction a where a.id = :id and a.usuario.id = :usuarioId")
    Optional<AssistantAction> findOwnedForUpdate(@Param("id") UUID id, @Param("usuarioId") Long usuarioId);
}
