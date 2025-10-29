package barbershopAPI.barbershopAPI.services;

import barbershopAPI.barbershopAPI.entities.Client;
import barbershopAPI.barbershopAPI.entities.SetPasswordToken;
import barbershopAPI.barbershopAPI.repositories.ClientRepository;
import barbershopAPI.barbershopAPI.repositories.SetPasswordTokenRepository;
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
public class SetPasswordService {
    
    private final SetPasswordTokenRepository tokenRepo;
    private final ClientRepository clientRepo;
    private final Mailer mailer;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${FRONTEND_BASE_URL:https://example.com}")
    String frontendBaseUrl;
    
    @Value("${SET_PASSWORD_TOKEN_TTL_HOURS:48}")
    int ttlHours;
    
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
     * Cria um token para o cliente definir senha e envia email
     */
    @Transactional
    public void sendSetPasswordEmail(Long clientId) {
        var client = clientRepo.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));
        
        if (client.getEmail() == null || client.getEmail().isBlank()) {
            throw new IllegalArgumentException("Cliente não tem email cadastrado");
        }
        
        var now = OffsetDateTime.now(ZoneOffset.UTC);
        var token = randomToken();
        var hash = sha256(token);
        
        var setPasswordToken = SetPasswordToken.builder()
                .tokenHash(hash)
                .clientId(clientId)
                .expiresAt(now.plusHours(ttlHours))
                .createdAt(now)
                .build();
        
        tokenRepo.save(setPasswordToken);
        
        var link = frontendBaseUrl.replaceAll("/+$", "") + "/definir-senha?token=" + token;
        mailer.sendSetPasswordEmail(client.getEmail(), client.getName(), link);
        
        log.info("Email de definição de senha enviado para cliente {}", clientId);
    }
    
    /**
     * Define a senha do cliente usando o token
     */
    @Transactional
    public Client setPassword(String token, String newPassword) {
        var hash = sha256(token);
        var setPasswordToken = tokenRepo.findByTokenHash(hash)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido"));
        
        if (setPasswordToken.getUsedAt() != null) {
            throw new IllegalStateException("Token já foi usado");
        }
        
        var now = OffsetDateTime.now(ZoneOffset.UTC);
        if (setPasswordToken.getExpiresAt().isBefore(now)) {
            throw new IllegalStateException("Token expirado");
        }
        
        var client = clientRepo.findById(setPasswordToken.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));
        
        // Define a senha
        client.setPassword(passwordEncoder.encode(newPassword));
        client = clientRepo.save(client);
        
        // Marca o token como usado
        setPasswordToken.setUsedAt(now);
        tokenRepo.save(setPasswordToken);
        
        log.info("Senha definida para cliente {}", client.getId());
        
        return client;
    }
    
    /**
     * Limpa tokens expirados (pode ser chamado por um scheduler)
     */
    @Transactional
    public int cleanupExpiredTokens() {
        var cutoffDate = OffsetDateTime.now(ZoneOffset.UTC);
        return tokenRepo.deleteExpiredTokens(cutoffDate);
    }
}

