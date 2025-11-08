package barbershopAPI.barbershopAPI.services;

import barbershopAPI.barbershopAPI.entities.Client;
import barbershopAPI.barbershopAPI.entities.PasswordResetToken;
import barbershopAPI.barbershopAPI.repositories.ClientRepository;
import barbershopAPI.barbershopAPI.repositories.PasswordResetTokenRepository;
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
public class PasswordResetService {
    private final PasswordResetTokenRepository tokenRepo;
    private final ClientRepository clientRepo;
    private final Mailer mailer;
    private final PasswordEncoder passwordEncoder;

    @Value("${FRONTEND_BASE_URL:https://example.com}")
    String frontendBaseUrl;

    @Value("${PASSWORD_RESET_TOKEN_TTL_MIN:30}")
    int ttlMin;

    private static final SecureRandom RNG = new SecureRandom();

    private static String randomToken() {
        var bytes = new byte[32];
        RNG.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String sha256(String s) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            var out = md.digest(s.getBytes(StandardCharsets.UTF_8));
            var sb = new StringBuilder();
            for (byte b : out) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Inicia processo de reset: cria token e envia email
     */
    @Transactional
    public void requestPasswordReset(String email) {
        // Verificar se o cliente existe
        var clientOpt = clientRepo.findByEmailIgnoreCase(email);
        if (clientOpt.isEmpty()) {
            // Por segurança, não revelar se o email existe ou não
            log.info("Password reset solicitado para email não existente: {}", email);
            return;
        }

        var now = OffsetDateTime.now(ZoneOffset.UTC);
        var token = randomToken();
        var hash = sha256(token);

        var resetToken = PasswordResetToken.builder()
                .tokenHash(hash)
                .email(email)
                .expiresAt(now.plusMinutes(ttlMin))
                .createdAt(now)
                .build();
        tokenRepo.save(resetToken);

        var link = frontendBaseUrl.replaceAll("/+$", "") + "/redefinir-senha?token=" + token;
        mailer.sendPasswordResetEmail(email, clientOpt.get().getName(), link);
        
        log.info("Email de reset de password enviado para: {}", email);
    }

    /**
     * Confirma token e redefine a password
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        var hash = sha256(token);
        var resetToken = tokenRepo.findByTokenHash(hash)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido ou expirado"));

        if (resetToken.getUsedAt() != null) {
            throw new IllegalStateException("Este link já foi utilizado");
        }

        var now = OffsetDateTime.now(ZoneOffset.UTC);
        if (resetToken.getExpiresAt().isBefore(now)) {
            throw new IllegalStateException("Este link expirou. Solicita um novo reset de password");
        }

        // Atualizar password do cliente
        var client = clientRepo.findByEmailIgnoreCase(resetToken.getEmail())
                .orElseThrow(() -> new IllegalStateException("Cliente não encontrado"));

        client.setPassword(passwordEncoder.encode(newPassword));
        clientRepo.save(client);

        // Marcar token como usado
        resetToken.setUsedAt(now);
        tokenRepo.save(resetToken);

        log.info("Password redefinida com sucesso para: {}", client.getEmail());
    }
}

