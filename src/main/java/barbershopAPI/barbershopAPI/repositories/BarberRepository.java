package barbershopAPI.barbershopAPI.repositories;

import barbershopAPI.barbershopAPI.entities.Barber;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BarberRepository extends JpaRepository<Barber, Long> {}
