package barbershopAPI.barbershopAPI.repositories;

import barbershopAPI.barbershopAPI.entities.TimeOff;
import org.springframework.data.jpa.repository.*;
import java.time.OffsetDateTime;
import java.util.List;

public interface TimeOffRepository extends JpaRepository<TimeOff, Long> {

    // usado no controller: listar por barbeiro num intervalo, ordenado
    List<TimeOff> findAllByBarberIdAndStartsAtBetweenOrderByStartsAtAsc(
            Long barberId, OffsetDateTime from, OffsetDateTime to);

    // usado no AvailabilityService: verificar overlap (startsAt < end && endsAt > start)
    boolean existsByBarberIdAndStartsAtLessThanAndEndsAtGreaterThan(
            Long barberId, OffsetDateTime end, OffsetDateTime start);
}
