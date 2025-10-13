// services/RegistrationService.java
package barbershopAPI.barbershopAPI.services;

import barbershopAPI.barbershopAPI.entities.Client;
import barbershopAPI.barbershopAPI.entities.RegistrationToken;
import barbershopAPI.barbershopAPI.repositories.ClientRepository;
import barbershopAPI.barbershopAPI.repositories.RegistrationTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationService {
    private final RegistrationTokenRepository tokenRepo;
    private final ClientRepository clientRepo;
    private final Mailer mailer;
    private final PasswordEncoder passwordEncoder; // garante que tens um encoder; se não usas Spring Security, cria um bean BCryptPasswordEncoder.

    @Value("${FRONTEND_BASE_URL:https://example.com}")
    String frontendBaseUrl;

    @Value("${EMAIL_VERIFY_TOKEN_TTL_MIN:60}")
    int ttlMin;

    private static final SecureRandom RNG = new SecureRandom();

    private static String randomToken(){
        var bytes = new byte[32];
        RNG.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    private static String sha256(String s){
        try {
            var md = MessageDigest.getInstance("SHA-256");
            var out = md.digest(s.getBytes(StandardCharsets.UTF_8));
            var sb = new StringBuilder();
            for (byte b: out) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    /** Inicia registo: cria token e envia email. NÃO cria Client ainda. */
    @Transactional
    public void startRegistration(String name, String phone, String email, String rawPassword){
        // Se já existir cliente com este email, podes devolver 409 ou simplesmente enviar nota “já existe”.
        if (clientRepo.findByEmailIgnoreCase(email).isPresent()) {
            // Idempotente: não reveles muito; aqui opto por simplesmente sair sem enviar nada.
            return;
        }

        var now = OffsetDateTime.now(ZoneOffset.UTC);
        var token = randomToken();
        var hash  = sha256(token);

        var rt = RegistrationToken.builder()
                .tokenHash(hash)
                .name(name)
                .phone(phone)
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .expiresAt(now.plusMinutes(ttlMin))
                .createdAt(now)
                .build();
        tokenRepo.save(rt);

        var link = frontendBaseUrl.replaceAll("/+$","") + "/confirmar?token=" + token;
        mailer.sendEmailVerification(email, link);
    }

    /** Confirma token e cria Client. */
    @Transactional
    public Client confirm(String token){
        var hash = sha256(token);
        var rt = tokenRepo.findByTokenHash(hash).orElseThrow(() -> new IllegalArgumentException("Token inválido"));
        if (rt.getUsedAt() != null) throw new IllegalStateException("Token já usado");
        var now = OffsetDateTime.now(ZoneOffset.UTC);
        if (rt.getExpiresAt().isBefore(now)) throw new IllegalStateException("Token expirado");

        // Se entretanto alguém criou a conta com este email, evita duplicados:
        if (clientRepo.findByEmailIgnoreCase(rt.getEmail()).isPresent()) {
            rt.setUsedAt(now); tokenRepo.save(rt);
            // devolve o cliente existente (ou lança erro 409, à escolha)
            return clientRepo.findByEmailIgnoreCase(rt.getEmail()).get();
        }

        var c = new Client();
        c.setName(rt.getName());
        c.setPhone(rt.getPhone());
        c.setEmail(rt.getEmail());
        c.setPassword(rt.getPasswordHash()); // o hash já está pronto
        // quaisquer defaults que uses…
        c = clientRepo.save(c);

        rt.setUsedAt(now);
        tokenRepo.save(rt);

        return c;
    }
}
