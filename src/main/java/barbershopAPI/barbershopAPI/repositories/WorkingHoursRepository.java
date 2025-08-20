package barbershopAPI.barbershopAPI.repositories;

import barbershopAPI.barbershopAPI.entities.WorkingHours;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.DayOfWeek;
import java.util.List;


public interface WorkingHoursRepository extends JpaRepository<WorkingHours, Long> {
    @Query("""
      select w from WorkingHours w
      where w.barber.id = :barberId and w.dayOfWeek = :dow
      order by w.startTime
    """)
    List<WorkingHours> findForDay(@Param("barberId") Long barberId, @Param("dow") DayOfWeek dow);

    // ✅ para listar tudo do barbeiro, ordenado
    List<WorkingHours> findByBarberIdOrderByDayOfWeekAscStartTimeAsc(Long barberId);

    List<WorkingHours> findAllForBarberOrdered(@Param("barberId") Long barberId);
}