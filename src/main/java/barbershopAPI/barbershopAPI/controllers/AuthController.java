// controllers/AuthController.java
package barbershopAPI.barbershopAPI.controllers;

import barbershopAPI.barbershopAPI.repositories.ClientRepository;
import barbershopAPI.barbershopAPI.services.JwtService;
import barbershopAPI.barbershopAPI.services.RegistrationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final ClientRepository clientRepo;
    private final RegistrationService registrationService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public record RegisterResponse(boolean sent){}
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest req){
        registrationService.startRegistration(req.name(), req.phone(), req.email(), req.password());
        return ResponseEntity.status(201).body(new RegisterResponse(true));
    }

    public record VerifyResponse(boolean verified, Long userId){}
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@Valid @RequestBody VerifyRequest req){
        try {
            var c = registrationService.confirm(req.token());
            return ResponseEntity.ok(new VerifyResponse(true, c.getId()));
        } catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(Map.of("verified", false, "message", "Token inválido."));
        } catch (IllegalStateException e){
            return ResponseEntity.badRequest().body(Map.of("verified", false, "message", e.getMessage()));
        }
    }


    public record LoginRequest(String email, String password){}
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req){
        var c = clientRepo.findByEmailIgnoreCase(req.email()).orElse(null);
        if (c == null || !passwordEncoder.matches(req.password(), c.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("message","Credenciais inválidas."));
        }
        
        String jwt = jwtService.generateToken(c.getEmail());
        String refreshToken = jwtService.generateRefreshToken(c.getEmail());
        return ResponseEntity.ok(Map.of(
                "token", jwt,
                "refreshToken", refreshToken,
                "user", Map.of("id", c.getId(), "name", c.getName(), "email", c.getEmail())
        ));
    }



    // REENVIAR EMAIL DE VERIFICAÇÃO
    public record ResendRequest(@Email String email){}
    @PostMapping("/verify/resend")
    public ResponseEntity<?> resendVerification(@Valid @RequestBody ResendRequest req){
        // Verificar se o email existe e está pendente de verificação
        var client = clientRepo.findByEmailIgnoreCase(req.email()).orElse(null);
        if (client == null) {
            // Por segurança, não revelar se o email existe ou não
            return ResponseEntity.ok(Map.of("message", "Se o email estiver registado, receberás um novo link de verificação."));
        }
        
        // Por agora, sempre reenviar (implementar lógica de verificação mais tarde)
        // TODO: Implementar verificação de email já verificado
        
        // Reenviar email de verificação (implementar lógica similar ao registo)
        // Por agora, retornar sucesso
        return ResponseEntity.ok(Map.of("message", "Se o email estiver registado, receberás um novo link de verificação."));
    }

    // REFRESH TOKEN
    public record RefreshRequest(@NotBlank String refreshToken){}
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshRequest req){
        try {
            // Validar refresh token
            if (!jwtService.validateToken(req.refreshToken())) {
                return ResponseEntity.status(401).body(Map.of("message", "Refresh token inválido."));
            }
            
            String email = jwtService.extractUsername(req.refreshToken());
            var c = clientRepo.findByEmailIgnoreCase(email).orElse(null);
            if (c == null) {
                return ResponseEntity.status(401).body(Map.of("message", "Utilizador não encontrado."));
            }
            
            // Gerar novos tokens
            String newJwt = jwtService.generateToken(c.getEmail());
            String newRefreshToken = jwtService.generateRefreshToken(c.getEmail());
            
            return ResponseEntity.ok(Map.of(
                    "token", newJwt,
                    "refreshToken", newRefreshToken,
                    "user", Map.of("id", c.getId(), "name", c.getName(), "email", c.getEmail())
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", "Refresh token inválido."));
        }
    }

    // DTOs (podes pôr em ficheiros próprios)
    public record RegisterRequest(@NotBlank String name, @NotBlank String phone, @Email String email, @NotBlank String password){}
    public record VerifyRequest(@NotBlank String token){}
}
