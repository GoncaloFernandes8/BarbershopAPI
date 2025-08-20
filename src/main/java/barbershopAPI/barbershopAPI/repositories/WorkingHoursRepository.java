package barbershopAPI.barbershopAPI.repositories;

import barbershopAPI.barbershopAPI.entities.WorkingHours;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.DayOfWeek;
import java.util.List;


public interface WorkingHoursRepository extends JpaRepository<WorkingHours, Long> {

    // listar todos os horários de um barbeiro, ordenados por dia e hora
    List<WorkingHours> findByBarber_IdOrderByDayOfWeekAscStartTimeAsc(Long barberId);

    // horários de UM dia específico, ordenados por hora
    List<WorkingHours> findByBarber_IdAndDayOfWeekOrderByStartTimeAsc(Long barberId, DayOfWeek dayOfWeek);
}