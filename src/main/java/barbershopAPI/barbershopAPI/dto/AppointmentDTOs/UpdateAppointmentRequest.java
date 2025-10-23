package barbershopAPI.barbershopAPI.dto.AppointmentDTOs;

import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;

public record UpdateAppointmentRequest(
        Long barberId,
        Long serviceId,
        Long clientId,
        OffsetDateTime startsAt,
        String notes
) {}

