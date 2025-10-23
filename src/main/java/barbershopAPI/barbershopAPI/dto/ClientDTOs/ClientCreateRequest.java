package barbershopAPI.barbershopAPI.dto.ClientDTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClientCreateRequest(
        @NotBlank(message = "Name is required") 
        String name, 
        
        @Pattern(regexp = "^\\+?[0-9\\s-]{7,}$", message = "Phone number must be valid") 
        String phone, 
        
        @NotBlank(message = "Email is required") 
        @Email(message = "Email must be valid") 
        String email, 
        
        @NotBlank(message = "Password is required") 
        @Size(min = 6, message = "Password must be at least 6 characters") 
        String password
) {}

