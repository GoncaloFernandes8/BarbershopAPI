package barbershopAPI.barbershopAPI.repositories;

import barbershopAPI.barbershopAPI.entities.Appointment;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.*;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    boolean existsByBarberIdAndIsActiveTrueAndStartsAtLessThanAndEndsAtGreaterThan(
            Long barberId, OffsetDateTime end, OffsetDateTime start);

    List<Appointment> findAllByBarberIdAndStartsAtBetween(
            Long barberId, OffsetDateTime from, OffsetDateTime to);

    List<Appointment> findAllByClientIdOrderByStartsAtDesc(Long clientId);

}
