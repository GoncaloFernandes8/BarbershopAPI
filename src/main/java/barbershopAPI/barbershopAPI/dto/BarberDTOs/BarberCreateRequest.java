package barbershopAPI.barbershopAPI.dto.BarberDTOs;

import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;

public record BarberCreateRequest(@NotBlank String name) {}
