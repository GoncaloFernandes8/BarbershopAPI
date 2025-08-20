package barbershopAPI.barbershopAPI.dto.AppointmentDTOs;

import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;

public record CreateAppointmentRequest(
        @NotNull Long barberId,
        @NotNull Long serviceId,
        @NotNull Long clientId,
        @NotNull OffsetDateTime startsAt,
        String notes
) {}
