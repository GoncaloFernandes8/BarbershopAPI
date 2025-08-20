package barbershopAPI.barbershopAPI.controllers;


import barbershopAPI.barbershopAPI.dto.WorkingHoursDTOs.WorkingHoursCreateRequest;
import barbershopAPI.barbershopAPI.dto.WorkingHoursDTOs.WorkingHoursResponse;
import barbershopAPI.barbershopAPI.entities.Barber;
import barbershopAPI.barbershopAPI.entities.WorkingHours;
import barbershopAPI.barbershopAPI.repositories.BarberRepository;
import barbershopAPI.barbershopAPI.repositories.WorkingHoursRepository;
import barbershopAPI.barbershopAPI.utils.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@RestController @RequestMapping("/working-hours") @RequiredArgsConstructor
public class WorkingHoursController {
    private final WorkingHoursRepository repo;
    private final BarberRepository barberRepo;

    @PostMapping
    public WorkingHoursResponse create(@Valid @RequestBody WorkingHoursCreateRequest req) {
        Barber b = barberRepo.findById(req.barberId()).orElseThrow(() -> new ResourceNotFoundException("Barber not found"));
        var wh = repo.save(WorkingHours.builder()
                .barber(b)
                .dayOfWeek(DayOfWeek.of(req.dayOfWeek()))
                .startTime(LocalTime.parse(req.startTime()))
                .endTime(LocalTime.parse(req.endTime()))
                .build());
        return new WorkingHoursResponse(wh.getId(), b.getId(), wh.getDayOfWeek().getValue(),
                wh.getStartTime().toString(), wh.getEndTime().toString());
    }

    @GetMapping
    public List<WorkingHoursResponse> listByBarber(@RequestParam Long barberId) {
        return repo.findByBarberIdOrderByDayOfWeekAscStartTimeAsc(barberId).stream()
                .map(wh -> new WorkingHoursResponse(wh.getId(), wh.getBarber().getId(),
                        wh.getDayOfWeek().getValue(), wh.getStartTime().toString(), wh.getEndTime().toString()))
                .toList();
    }

    @DeleteMapping("/{id}") public void delete(@PathVariable Long id) { repo.deleteById(id); }
}