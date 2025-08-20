package barbershopAPI.barbershopAPI.repositories;

import barbershopAPI.barbershopAPI.entities.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {}