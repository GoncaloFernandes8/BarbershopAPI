package barbershopAPI.barbershopAPI.dto.AppointmentDTOs;

import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotNull String status
) {}

