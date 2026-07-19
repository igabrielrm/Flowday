package com.flowday.flowday.repository;

import com.flowday.flowday.model.MobileOAuthCode;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MobileOAuthCodeRepository extends JpaRepository<MobileOAuthCode, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from MobileOAuthCode c join fetch c.usuario where c.codeHash = :hash")
    Optional<MobileOAuthCode> findByCodeHashForUpdate(@Param("hash") String hash);
}
