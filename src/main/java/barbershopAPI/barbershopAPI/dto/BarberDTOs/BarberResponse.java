package barbershopAPI.barbershopAPI.dto.BarberDTOs;

import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;

public record BarberResponse(Long id, String name, boolean active, OffsetDateTime createdAt) {}