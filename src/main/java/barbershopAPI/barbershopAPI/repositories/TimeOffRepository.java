package barbershopAPI.barbershopAPI.repositories;

import barbershopAPI.barbershopAPI.entities.TimeOff;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.OffsetDateTime;

public interface TimeOffRepository extends JpaRepository<TimeOff, Long> {
    @Query("""
    select (count(t) > 0) from TimeOff t
    where t.barber.id = :barberId and t.startsAt < :end and t.endsAt > :start
  """)
    boolean existsOverlap(@Param("barberId") Long barberId,
                          @Param("start") OffsetDateTime start,
                          @Param("end") OffsetDateTime end);
}
