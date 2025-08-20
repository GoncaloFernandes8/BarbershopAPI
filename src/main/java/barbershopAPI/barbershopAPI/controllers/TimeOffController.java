package barbershopAPI.barbershopAPI.controllers;

import barbershopAPI.barbershopAPI.dto.TimeOffDTOs.TimeOffCreateRequest;
import barbershopAPI.barbershopAPI.dto.TimeOffDTOs.TimeOffResponse;
import barbershopAPI.barbershopAPI.entities.*;
import barbershopAPI.barbershopAPI.repositories.*;
import barbershopAPI.barbershopAPI.utils.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime;
import java.util.List;

@RestController @RequestMapping("/time-off") @RequiredArgsConstructor
public class TimeOffController {
    private final TimeOffRepository repo;
    private final BarberRepository barberRepo;

    @PostMapping
    public TimeOffResponse create(@Valid @RequestBody TimeOffCreateRequest req) {
        Barber b = barberRepo.findById(req.barberId()).orElseThrow(() -> new ResourceNotFoundException("Barber not found"));
        var t = repo.save(TimeOff.builder().barber(b).startsAt(req.startsAt()).endsAt(req.endsAt()).reason(req.reason()).build());
        return new TimeOffResponse(t.getId(), b.getId(), t.getStartsAt(), t.getEndsAt(), t.getReason());
    }

    @GetMapping
    public List<TimeOffResponse> list(@RequestParam Long barberId,
                                      @RequestParam OffsetDateTime from,
                                      @RequestParam OffsetDateTime to) {
        return repo.findAllByBarberIdAndStartsAtBetweenOrderByStartsAtAsc(barberId, from, to).stream()
                .map(t -> new TimeOffResponse(t.getId(), t.getBarber().getId(), t.getStartsAt(), t.getEndsAt(), t.getReason()))
                .toList();
    }

    @DeleteMapping("/{id}") public void delete(@PathVariable Long id) { repo.deleteById(id); }
}
