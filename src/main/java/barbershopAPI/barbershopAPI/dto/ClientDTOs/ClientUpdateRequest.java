package barbershopAPI.barbershopAPI.dto.ClientDTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ClientUpdateRequest(
        @NotBlank String name, 
        @Pattern(regexp = "^\\+?[0-9\\s-]{7,}$", message = "Phone number must be valid") 
        String phone
) {}
