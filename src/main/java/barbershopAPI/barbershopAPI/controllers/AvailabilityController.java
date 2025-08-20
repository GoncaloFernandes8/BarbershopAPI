package barbershopAPI.barbershopAPI.controllers;

import barbershopAPI.barbershopAPI.services.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@RestController @RequestMapping("/availability") @RequiredArgsConstructor
public class AvailabilityController {
    private final AvailabilityService availabilityService;
    @GetMapping
    public List<OffsetDateTime> get(@RequestParam Long barberId,
                                    @RequestParam Long serviceId,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return availabilityService.getAvailableStarts(barberId, serviceId, date);
    }
}
