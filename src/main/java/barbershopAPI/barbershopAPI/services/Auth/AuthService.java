package barbershopAPI.barbershopAPI.services.Auth;



import barbershopAPI.barbershopAPI.dto.*;
import barbershopAPI.barbershopAPI.dto.AuthDTOs.AuthUserDto;
import barbershopAPI.barbershopAPI.dto.AuthDTOs.LoginRequest;
import barbershopAPI.barbershopAPI.dto.AuthDTOs.LoginResponse;
import barbershopAPI.barbershopAPI.entities.Client;
import barbershopAPI.barbershopAPI.repositories.ClientRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AuthService {

    private final ClientRepository clients;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final Key key;
    private final long expMinutes;

    public AuthService(
            ClientRepository clients,
            // Lê diretamente das variáveis de ambiente do Koyeb:
            @Value("${JWT_SECRET}") String secret,
            @Value("${JWT_EXP_MINUTES:120}") long expMinutes
    ) {
        this.clients = clients;
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expMinutes = expMinutes;
    }

    public LoginResponse login(LoginRequest req) {
        Client c = clients.findByEmail(req.email())
                .orElseThrow(() -> new RuntimeException("Credenciais inválidas"));

        String stored = c.getPassword();
        boolean ok = false;
        if (stored != null && stored.startsWith("$2")) {
            // BCrypt
            ok = encoder.matches(req.password(), stored);
        } else {
            // Texto simples (não recomendado; migra quando puderes)
            ok = req.password().equals(stored);
        }
        if (!ok) throw new RuntimeException("Credenciais inválidas");

        String token = Jwts.builder()
                .setSubject(String.valueOf(c.getId()))
                .claim("name", c.getName())
                .claim("email", c.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(expMinutes, ChronoUnit.MINUTES)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        AuthUserDto user = new AuthUserDto(String.valueOf(c.getId()), c.getName(), c.getEmail(), "APPROVED");
        return new LoginResponse(token, user);
    }
}
