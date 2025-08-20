package barbershopAPI.barbershopAPI.dto.ClientDTOs;

import jakarta.validation.constraints.NotBlank;

public record ClientUpdateRequest(@NotBlank String name, String phone) {}
