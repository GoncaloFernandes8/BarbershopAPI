package barbershopAPI.barbershopAPI.dto.TimeOffDTOs;


import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;

public record TimeOffCreateRequest(
        @NotNull Long barberId,
        @NotNull OffsetDateTime startsAt,
        @NotNull OffsetDateTime endsAt,
        String reason
) {}
