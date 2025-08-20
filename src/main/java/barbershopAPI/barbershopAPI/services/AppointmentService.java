package barbershopAPI.barbershopAPI.services;

import barbershopAPI.barbershopAPI.entities.*;
import barbershopAPI.barbershopAPI.repositories.*;
import barbershopAPI.barbershopAPI.dto.AppointmentDTOs.AppointmentResponse;
import barbershopAPI.barbershopAPI.dto.AppointmentDTOs.CreateAppointmentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepo;
    private final BarberRepository barberRepo;
    private final ServiceRepository serviceRepo;
    private final ClientRepository clientRepo;

    @Transactional
    public AppointmentResponse create(CreateAppointmentRequest req) {
        var barber  = barberRepo.findById(req.barberId()).orElseThrow();
        var service = serviceRepo.findById(req.serviceId()).orElseThrow();
        var client  = clientRepo.findById(req.clientId()).orElseThrow();

        int mins = service.getDurationMin() + (service.getBufferAfterMin() == null ? 0 : service.getBufferAfterMin());
        OffsetDateTime startsAt = req.startsAt();
        OffsetDateTime endsAt   = startsAt.plusMinutes(mins);

        // pré-checagem rápida de conflito (melhor UX)
        boolean busy = appointmentRepo.existsByBarberIdAndIsActiveTrueAndStartsAtLessThanAndEndsAtGreaterThan(barber.getId(), startsAt, endsAt);
        if (busy) throw new SlotConflictException("Slot já ocupado para este barbeiro");

        var appt = Appointment.builder()
                .barber(barber)
                .service(service)
                .client(client)
                .startsAt(startsAt)
                .endsAt(endsAt)
                .notes(req.notes())
                .build();

        try {
            appt = appointmentRepo.save(appt);
        } catch (DataIntegrityViolationException e) {
            // fallback caso a constraint do DB dispare em corrida
            throw new SlotConflictException("Slot já ocupado para este barbeiro");
        }

        return new AppointmentResponse(
                appt.getId(),
                barber.getId(),
                service.getId(),
                client.getId(),
                appt.getStartsAt(),
                appt.getEndsAt(),
                appt.getStatus().name(),
                appt.getNotes()
        );
    }

    public static class SlotConflictException extends RuntimeException {
        public SlotConflictException(String message) { super(message); }
    }
}
