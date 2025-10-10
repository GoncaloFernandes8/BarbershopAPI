// services/EmailVerificationService.java
package barbershopAPI.barbershopAPI.services;

import barbershopAPI.barbershopAPI.entities.Client; // ajusta se o teu modelo se chamar diferente
import barbershopAPI.barbershopAPI.entities.EmailVerificationToken;
import barbershopAPI.barbershopAPI.repositories.ClientRepository;
import barbershopAPI.barbershopAPI.repositories.EmailVerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    private final EmailVerificationTokenRepository tokenRepo;
    private final ClientRepository clientRepo; // ajusta o repos conforme o teu
    private final Mailer mailer;

    @Value("${FRONTEND_BASE_URL:https://example.com}")
    String frontendBaseUrl;

    @Value("${EMAIL_VERIFY_TOKEN_TTL_MIN:60}")
    int ttlMin;

    @Value("${EMAIL_VERIFY_RESEND_COOLDOWN_MIN:10}")
    int resendCooldownMin;

    private static final SecureRandom RNG = new SecureRandom();

    private static String randomToken(){
        var bytes = new byte[32];
        RNG.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    public boolean isUserVerified(Long userId){
        // verificado se existir algum token usado por este user
        return tokenRepo.existsByUserIdAndUsedAtIsNotNull(userId);
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

    @Transactional
    public void createAndSendToken(Long userId) {
        var user = clientRepo.findById(userId).orElseThrow();

        // se já verificado, não enviar
        if (isUserVerified(userId)) return;

        String token = randomToken();
        String hash = sha256(token);
        var now = OffsetDateTime.now(ZoneOffset.UTC);

        var t = EmailVerificationToken.builder()
                .userId(userId)
                .tokenHash(hash)
                .expiresAt(now.plusMinutes(ttlMin))
                .createdAt(now)
                .build();
        tokenRepo.save(t);

        String verifyUrl = frontendBaseUrl.replaceAll("/+$","") + "/confirmar?token=" + token;
        mailer.sendEmailVerification(user.getEmail(), verifyUrl);
    }

    @Transactional
    public boolean verify(String token){
        String hash = sha256(token);
        var t = tokenRepo.findByTokenHash(hash).orElseThrow(() -> new IllegalArgumentException("Token inválido"));

        if (t.getUsedAt() != null) throw new IllegalStateException("Token já usado");
        var now = OffsetDateTime.now(ZoneOffset.UTC);
        if (t.getExpiresAt().isBefore(now)) throw new IllegalStateException("Token expirado");

        t.setUsedAt(now);
        tokenRepo.save(t);
        return true;
    }

    @Transactional
    public void resend(String email){
        var opt = clientRepo.findByEmailIgnoreCase(email);
        if (opt.isEmpty()) return;              // não vazar se o email existe
        var user = opt.get();

        // se já estiver verificado (algum token usado), não reenviar
        if (isUserVerified(user.getId())) return;

        // gera novo token e envia
        createAndSendToken(user.getId());
    }
}
