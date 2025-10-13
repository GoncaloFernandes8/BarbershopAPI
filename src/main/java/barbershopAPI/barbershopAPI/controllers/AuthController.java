// controllers/AuthController.java
package barbershopAPI.barbershopAPI.controllers;

import barbershopAPI.barbershopAPI.entities.Client;
import barbershopAPI.barbershopAPI.repositories.ClientRepository;
import barbershopAPI.barbershopAPI.services.EmailVerificationService;
import barbershopAPI.barbershopAPI.services.RegistrationService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final ClientRepository clientRepo;
    private final RegistrationService registrationService;
    private final EmailVerificationService emailVerification;

    public record RegisterResponse(boolean sent){}
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest req){
        registrationService.startRegistration(req.name(), req.phone(), req.email(), req.password());
        return ResponseEntity.status(201).body(new RegisterResponse(true));
    }

    public record VerifyResponse(boolean verified, Long userId){}
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody VerifyRequest req){
        try {
            var c = registrationService.confirm(req.token());
            return ResponseEntity.ok(new VerifyResponse(true, c.getId()));
        } catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(Map.of("verified", false, "message", "Token inválido."));
        } catch (IllegalStateException e){
            return ResponseEntity.badRequest().body(Map.of("verified", false, "message", e.getMessage()));
        }
    }

    // === Reenviar ===
    @PostMapping("/verify/resend")
    public ResponseEntity<?> resend(@RequestBody ResendRequest req){
        emailVerification.resend(req.email());
        return ResponseEntity.noContent().build();
    }

    // LOGIN agora é clássico: procurar pelo email e validar password. Sem verificação aqui.
    public record LoginRequest(String email, String password){}
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req){
        var c = clientRepo.findByEmailIgnoreCase(req.email()).orElse(null);
        if (c == null /* || !passwordMatches */) {
            return ResponseEntity.status(401).body(Map.of("message","Credenciais inválidas."));
        }
        // ...gera o teu JWT e responde
        return ResponseEntity.ok(Map.of(
                "token","<jwt>",
                "user", Map.of("id", c.getId(), "name", c.getName(), "email", c.getEmail())
        ));
    }



    // DTOs (podes pôr em ficheiros próprios)
    public record RegisterRequest(@NotBlank String name, @NotBlank String phone, @Email String email, @NotBlank String password){}
    public record VerifyRequest(@NotBlank String token){}
    public record ResendRequest(@Email String email){}
}
