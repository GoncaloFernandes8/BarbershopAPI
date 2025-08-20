package barbershopAPI.barbershopAPI.services;

import barbershopAPI.barbershopAPI.entities.ServiceEntity;
import barbershopAPI.barbershopAPI.entities.WorkingHours;

import barbershopAPI.barbershopAPI.repositories.WorkingHoursRepository;
import barbershopAPI.barbershopAPI.repositories.AppointmentRepository;
import barbershopAPI.barbershopAPI.repositories.TimeOffRepository;
import barbershopAPI.barbershopAPI.repositories.ServiceRepository;



import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final WorkingHoursRepository workingHoursRepo;
    private final AppointmentRepository appointmentRepo;
    private final TimeOffRepository timeOffRepo;
    private final ServiceRepository serviceRepo;

    private static final ZoneId TZ = ZoneId.of("Europe/Lisbon");
    private static final int STEP_MIN = 15;

    public List<OffsetDateTime> getAvailableStarts(Long barberId, Long serviceId, LocalDate day) {
        ServiceEntity service = serviceRepo.findById(serviceId).orElseThrow();
        int buffer = service.getBufferAfterMin() == null ? 0 : service.getBufferAfterMin();
        int neededMin = service.getDurationMin() + buffer;
        Duration needed = Duration.ofMinutes(neededMin);

        List<WorkingHours> whs = workingHoursRepo.findForDay(barberId, day.getDayOfWeek());
        List<OffsetDateTime> slots = new ArrayList<>();
        ZonedDateTime nowLisbon = ZonedDateTime.now(TZ);

        for (WorkingHours wh : whs) {
            ZonedDateTime blockStart = day.atTime(wh.getStartTime()).atZone(TZ);
            ZonedDateTime blockEnd   = day.atTime(wh.getEndTime()).atZone(TZ);

            ZonedDateTime cand = blockStart;
            int mod = cand.getMinute() % STEP_MIN;
            if (mod != 0) cand = cand.plusMinutes(STEP_MIN - mod);

            for (; !cand.plus(needed).isAfter(blockEnd); cand = cand.plusMinutes(STEP_MIN)) {
                if (cand.isBefore(nowLisbon)) continue;

                OffsetDateTime s = cand.toOffsetDateTime();
                OffsetDateTime e = cand.plus(needed).toOffsetDateTime();

                boolean busy = appointmentRepo.existsActiveOverlap(barberId, s, e)
                        || timeOffRepo.existsOverlap(barberId, s, e);
                if (!busy) slots.add(s);
            }
        }
        return slots.stream().sorted().collect(Collectors.toList());
    }
}
