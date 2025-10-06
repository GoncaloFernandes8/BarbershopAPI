package barbershopAPI.barbershopAPI.dto.ClientDTOs;

import jakarta.validation.constraints.NotBlank;

public record ClientCreateRequest(@NotBlank String name, String phone, String email, String password) {}

