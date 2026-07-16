package com.uce.servidorproyecto.repository;

import com.uce.servidorproyecto.model.MobileRefreshToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface MobileRefreshTokenRepository extends JpaRepository<MobileRefreshToken, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select token from MobileRefreshToken token join fetch token.usuario where token.tokenHash = :hash")
    Optional<MobileRefreshToken> findByTokenHashForUpdate(@Param("hash") String hash);

    @Modifying
    @Query("update MobileRefreshToken token set token.revokedAt = :now "
            + "where token.usuario.id = :userId and token.revokedAt is null")
    int revokeAllForUser(@Param("userId") Long userId, @Param("now") Instant now);
}
