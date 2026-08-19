package com.merging.chunks.repo;

import com.merging.chunks.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken, UUID> {

    @Query("SELECT r FROM RefreshToken r WHERE r.token_hash = :refreshToken AND r.revoked = false")
    Optional<RefreshToken> findByTokenHashNotRevoked(@Param("refreshToken") String refreshToken);
}
