package barbershopAPI.barbershopAPI.dto.ServiceDTOs;

import jakarta.validation.constraints.*;

public record ServiceUpdateRequest(
        @NotBlank String name,
        @NotNull @Min(1) Integer durationMin,
        @NotNull @Min(0) Integer bufferAfterMin,
        @Min(0) Integer priceCents,
        Boolean active
) {}
