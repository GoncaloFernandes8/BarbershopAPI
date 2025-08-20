package barbershopAPI.barbershopAPI.controllers;


import barbershopAPI.barbershopAPI.services.AppointmentService;
import barbershopAPI.barbershopAPI.dto.AppointmentResponse;
import barbershopAPI.barbershopAPI.dto.CreateAppointmentRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public AppointmentResponse create(@Valid @RequestBody CreateAppointmentRequest req) {
        return appointmentService.create(req);
    }
}
