package barbershopAPI.barbershopAPI.dto.BarberDTOs;


import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;

public record BarberUpdateRequest(@NotBlank String name, Boolean active) {}
