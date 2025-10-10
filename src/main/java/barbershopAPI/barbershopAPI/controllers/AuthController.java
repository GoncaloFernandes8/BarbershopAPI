// controllers/AuthController.java
package barbershopAPI.barbershopAPI.controllers;

import barbershopAPI.barbershopAPI.entities.Client;
import barbershopAPI.barbershopAPI.repositories.ClientRepository;
import barbershopAPI.barbershopAPI.services.EmailVerificationService;
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
    private final EmailVerificationService emailVerification;

    // === Registo (simplificado; usa o teu existente e chama createAndSendToken) ===
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req){
        // validações e encriptação password (usa as tuas)
        var c = new Client();
        c.setName(req.name());
        c.setPhone(req.phone());
        c.setEmail(req.email());
        c.setPassword(req.password()); // TROCA PARA HASHED
        c = clientRepo.save(c);

        emailVerification.createAndSendToken(c.getId());
        return ResponseEntity.status(201).body(Map.of(
                "userId", c.getId(),
                "requiresEmailVerification", true
        ));
    }

    // === Verificar token ===
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody VerifyRequest req){
        try {
            emailVerification.verify(req.token());
            return ResponseEntity.ok(Map.of("verified", true));
        } catch (IllegalArgumentException e){
            return ResponseEntity.status(400).body(Map.of("verified", false, "message", "Token inválido."));
        } catch (IllegalStateException e){
            return ResponseEntity.status(400).body(Map.of("verified", false, "message", e.getMessage()));
        }
    }

    // === Reenviar ===
    @PostMapping("/verify/resend")
    public ResponseEntity<?> resend(@RequestBody ResendRequest req){
        emailVerification.resend(req.email());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req){
        var c = clientRepo.findByEmailIgnoreCase(req.email()).orElse(null);
        if (c == null /* || !passwordMatches(...) */) {
            return ResponseEntity.status(401).body(Map.of("message","Credenciais inválidas."));
        }

        // apenas verifica estado; NÃO envia email aqui
        boolean verified = emailVerification.isUserVerified(c.getId());
        if (!verified){
            return ResponseEntity.status(403).body(Map.of(
                    "code","EMAIL_NOT_VERIFIED",
                    "message","Confirma o teu email para entrar. Se precisares, pede o reenvio."
            ));
        }

        // ...gera token/JWT como já fazes
        return ResponseEntity.ok(Map.of(
                "token","<jwt>",
                "user", Map.of("id", c.getId(), "name", c.getName(), "email", c.getEmail())
        ));
    }


    // DTOs (podes pôr em ficheiros próprios)
    public record RegisterRequest(@NotBlank String name, @NotBlank String phone, @Email String email, @NotBlank String password){}
    public record VerifyRequest(@NotBlank String token){}
    public record ResendRequest(@Email String email){}
    public record LoginRequest(@Email String email, @NotBlank String password){}
}
