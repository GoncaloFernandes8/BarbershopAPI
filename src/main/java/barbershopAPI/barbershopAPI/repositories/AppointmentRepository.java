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

    List<Appointment> findAllByBarberIdAndIsActiveTrueAndStartsAtLessThanAndEndsAtGreaterThan(
            Long barberId, OffsetDateTime end, OffsetDateTime start);

    List<Appointment> findAllByBarberIdAndStartsAtBetween(
            Long barberId, OffsetDateTime from, OffsetDateTime to);

    List<Appointment> findAllByClientIdOrderByStartsAtDesc(Long clientId);

    // Para o scheduler de lembretes - com JOIN FETCH para evitar LazyInitializationException
    @Query("SELECT DISTINCT a FROM Appointment a " +
           "LEFT JOIN FETCH a.client " +
           "LEFT JOIN FETCH a.barber " +
           "LEFT JOIN FETCH a.service " +
           "WHERE a.isActive = true " +
           "AND a.startsAt BETWEEN :start AND :end")
    List<Appointment> findAllByIsActiveTrueAndStartsAtBetween(
            @Param("start") OffsetDateTime startWindow, 
            @Param("end") OffsetDateTime endWindow);
    
    List<Appointment> findAllByStartsAtBefore(OffsetDateTime cutoffTime);
    
    // Para estatísticas do dashboard
    List<Appointment> findAllByStartsAtBetween(OffsetDateTime from, OffsetDateTime to);

}
