package barbershopAPI.barbershopAPI.repositories;

import barbershopAPI.barbershopAPI.entities.SetPasswordToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface SetPasswordTokenRepository extends JpaRepository<SetPasswordToken, Long> {
    
    Optional<SetPasswordToken> findByTokenHash(String tokenHash);
    
    Optional<SetPasswordToken> findByClientIdAndUsedAtIsNull(Long clientId);
    
    @Modifying
    @Query("DELETE FROM SetPasswordToken t WHERE t.expiresAt < :cutoffDate")
    int deleteExpiredTokens(@Param("cutoffDate") OffsetDateTime cutoffDate);
}

