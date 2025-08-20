package barbershopAPI.barbershopAPI.dto;


import java.time.OffsetDateTime;
import java.util.UUID;

public record AppointmentResponse(
        UUID id,
        Long barberId,
        Long serviceId,
        Long clientId,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        String status,
        String notes
) {}