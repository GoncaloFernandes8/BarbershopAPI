package barbershopAPI.barbershopAPI.services;
import barbershopAPI.barbershopAPI.entities.*;
import barbershopAPI.barbershopAPI.repositories.*;
import barbershopAPI.barbershopAPI.dto.AppointmentDTOs.AppointmentResponse;
import barbershopAPI.barbershopAPI.dto.AppointmentDTOs.CreateAppointmentRequest;
import barbershopAPI.barbershopAPI.dto.AppointmentDTOs.UpdateAppointmentRequest;
import barbershopAPI.barbershopAPI.utils.ResourceNotFoundException;
import barbershopAPI.barbershopAPI.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final Mailer mailer;
    private final AppointmentRepository appointmentRepo;
    private final BarberRepository barberRepo;
    private final ServiceRepository serviceRepo;
    private final ClientRepository clientRepo;
    private final TimeOffRepository timeOffRepo;
    private final NotificationService notificationService;

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
        
        // Verificar se o barbeiro está de folga neste período
        if (isBarberOnTimeOff(barber.getId(), startsAt, endsAt)) {
            throw new TimeOffConflictException("O barbeiro está de folga neste período");
        }

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
            throw new SlotConflictException("Slot já ocupado para este barbeiro");
        }

        try {
            mailer.sendAppointmentConfirmation(client.getEmail(), appt, service, barber);
        } catch (Exception ex) {
            log.warn("Falha ao enviar email de confirmação para {} (appt {}).", client.getEmail(), appt.getId(), ex);
        }

        // Create notification for new appointment
        try {
            String timeStr = appt.getStartsAt().toLocalTime().toString();
            notificationService.notifyNewAppointment(client.getName(), barber.getName(), timeStr);
        } catch (Exception ex) {
            log.warn("Falha ao criar notificação para novo agendamento {}: {}", appt.getId(), ex.getMessage());
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

    @Transactional
    public AppointmentResponse update(java.util.UUID id, UpdateAppointmentRequest req) {
        var appt = appointmentRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        // Se o appointment já foi cancelado, não permitir edição
        if (appt.getStatus() == barbershopAPI.barbershopAPI.enums.AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Cannot update a cancelled appointment");
        }

        boolean changed = false;

        // Atualizar barbeiro se fornecido
        if (req.barberId() != null && !req.barberId().equals(appt.getBarber().getId())) {
            var barber = barberRepo.findById(req.barberId()).orElseThrow();
            appt.setBarber(barber);
            changed = true;
        }

        // Atualizar serviço se fornecido
        ServiceEntity service = appt.getService();
        if (req.serviceId() != null && !req.serviceId().equals(service.getId())) {
            service = serviceRepo.findById(req.serviceId()).orElseThrow();
            appt.setService(service);
            changed = true;
        }

        // Atualizar cliente se fornecido
        if (req.clientId() != null && !req.clientId().equals(appt.getClient().getId())) {
            var client = clientRepo.findById(req.clientId()).orElseThrow();
            appt.setClient(client);
            changed = true;
        }

        // Atualizar data/hora se fornecido
        if (req.startsAt() != null && !req.startsAt().equals(appt.getStartsAt())) {
            int mins = service.getDurationMin() + (service.getBufferAfterMin() == null ? 0 : service.getBufferAfterMin());
            OffsetDateTime newStartsAt = req.startsAt();
            OffsetDateTime newEndsAt = newStartsAt.plusMinutes(mins);

            // Verificar conflito (excluindo o próprio appointment)
            boolean busy = appointmentRepo.existsByBarberIdAndIsActiveTrueAndStartsAtLessThanAndEndsAtGreaterThan(
                    appt.getBarber().getId(), newStartsAt, newEndsAt);
            
            if (busy) {
                // Verificar se o conflito não é com o próprio appointment
                var conflicting = appointmentRepo.findAllByBarberIdAndIsActiveTrueAndStartsAtLessThanAndEndsAtGreaterThan(
                        appt.getBarber().getId(), newEndsAt, newStartsAt);
                
                boolean hasRealConflict = conflicting.stream()
                        .anyMatch(a -> !a.getId().equals(id));
                
                if (hasRealConflict) {
                    throw new SlotConflictException("Slot já ocupado para este barbeiro");
                }
            }
            
            // Verificar se o barbeiro está de folga no novo período
            if (isBarberOnTimeOff(appt.getBarber().getId(), newStartsAt, newEndsAt)) {
                throw new TimeOffConflictException("O barbeiro está de folga neste período");
            }

            appt.setStartsAt(newStartsAt);
            appt.setEndsAt(newEndsAt);
            changed = true;
        }

        // Atualizar notas se fornecido (mesmo que seja vazio)
        if (req.notes() != null && !req.notes().equals(appt.getNotes())) {
            appt.setNotes(req.notes());
            changed = true;
        }

        if (changed) {
            try {
                appt = appointmentRepo.save(appt);
            } catch (DataIntegrityViolationException e) {
                throw new SlotConflictException("Slot já ocupado para este barbeiro");
            }
        }

        return new AppointmentResponse(
                appt.getId(),
                appt.getBarber().getId(),
                appt.getService().getId(),
                appt.getClient().getId(),
                appt.getStartsAt(),
                appt.getEndsAt(),
                appt.getStatus().name(),
                appt.getNotes()
        );
    }

    /**
     * Verifica se o barbeiro está de folga no período especificado
     */
    private boolean isBarberOnTimeOff(Long barberId, OffsetDateTime startsAt, OffsetDateTime endsAt) {
        var timeOffs = timeOffRepo.findAllByBarberIdAndStartsAtLessThanAndEndsAtGreaterThan(
                barberId, endsAt, startsAt);
        return !timeOffs.isEmpty();
    }

    public static class SlotConflictException extends RuntimeException {
        public SlotConflictException(String message) { super(message); }
    }
    
    public static class TimeOffConflictException extends RuntimeException {
        public TimeOffConflictException(String message) { super(message); }
    }
}
