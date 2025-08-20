package barbershopAPI.barbershopAPI.controllers;


import barbershopAPI.barbershopAPI.dto.AppointmentDTOs.AppointmentResponse;
import barbershopAPI.barbershopAPI.dto.AppointmentDTOs.CreateAppointmentRequest;
import barbershopAPI.barbershopAPI.enums.AppointmentStatus;
import barbershopAPI.barbershopAPI.entities.Appointment;
import barbershopAPI.barbershopAPI.repositories.AppointmentRepository;
import barbershopAPI.barbershopAPI.services.AppointmentService;
import barbershopAPI.barbershopAPI.utils.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController @RequestMapping("/appointments") @RequiredArgsConstructor
public class AppointmentController {
    private final AppointmentService appointmentService;
    private final AppointmentRepository appointmentRepo;

    @PostMapping
    public AppointmentResponse create(@Valid @RequestBody CreateAppointmentRequest req) {
        return appointmentService.create(req);
    }

    @GetMapping
    public List<AppointmentResponse> list(@RequestParam Long barberId,
                                          @RequestParam OffsetDateTime from,
                                          @RequestParam OffsetDateTime to) {
        return appointmentRepo.findAllByBarberIdAndStartsAtBetween(barberId, from, to).stream()
                .map(a -> new AppointmentResponse(a.getId(), a.getBarber().getId(), a.getService().getId(),
                        a.getClient().getId(), a.getStartsAt(), a.getEndsAt(), a.getStatus().name(), a.getNotes()))
                .toList();
    }

    @PatchMapping("/{id}/cancel")
    public AppointmentResponse cancel(@PathVariable UUID id) {
        Appointment a = appointmentRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        a.setStatus(AppointmentStatus.CANCELLED);
        a.setActive(false);
        a = appointmentRepo.save(a);
        return new AppointmentResponse(a.getId(), a.getBarber().getId(), a.getService().getId(),
                a.getClient().getId(), a.getStartsAt(), a.getEndsAt(), a.getStatus().name(), a.getNotes());
    }
}