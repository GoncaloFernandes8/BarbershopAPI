package barbershopAPI.barbershopAPI.dto.WorkingHoursDTOs;

import jakarta.validation.constraints.*;

public record WorkingHoursCreateRequest(
        @NotNull Long barberId,
        @NotNull @Min(1) @Max(7) Integer dayOfWeek,
        @NotBlank String startTime, // "HH:mm"
        @NotBlank String endTime    // "HH:mm"
) {}
