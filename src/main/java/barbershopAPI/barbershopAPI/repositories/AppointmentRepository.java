package barbershopAPI.barbershopAPI.repositories;

import barbershopAPI.barbershopAPI.entities.Appointment;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.*;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    @Query("""
    select (count(a) > 0) from Appointment a
    where a.barber.id = :barberId and a.isActive = true
      and a.startsAt < :end and a.endsAt > :start
  """)
    boolean existsActiveOverlap(@Param("barberId") Long barberId,
                                @Param("start") OffsetDateTime start,
                                @Param("end") OffsetDateTime end);

    List<Appointment> findAllByBarberIdAndStartsAtBetween(Long barberId, OffsetDateTime from, OffsetDateTime to);
}
