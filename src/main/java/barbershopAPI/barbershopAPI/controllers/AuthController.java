package barbershopAPI.barbershopAPI.controllers;




import barbershopAPI.barbershopAPI.dto.AuthDTOs.LoginRequest;
import barbershopAPI.barbershopAPI.dto.AuthDTOs.LoginResponse;
import barbershopAPI.barbershopAPI.services.Auth.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        try {
            return ResponseEntity.ok(auth.login(req));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(401).body(null);
        }
    }
}


