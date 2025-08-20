package barbershopAPI.barbershopAPI.repositories;

import barbershopAPI.barbershopAPI.entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {}
