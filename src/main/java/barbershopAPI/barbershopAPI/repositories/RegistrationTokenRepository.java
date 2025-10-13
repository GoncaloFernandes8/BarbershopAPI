// repositories/RegistrationTokenRepository.java
package barbershopAPI.barbershopAPI.repositories;

import barbershopAPI.barbershopAPI.entities.RegistrationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RegistrationTokenRepository extends JpaRepository<RegistrationToken, UUID> {
    Optional<RegistrationToken> findByTokenHash(String tokenHash);
}
