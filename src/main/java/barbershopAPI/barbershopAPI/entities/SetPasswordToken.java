package barbershopAPI.barbershopAPI.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "set_password_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SetPasswordToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;
    
    @Column(name = "client_id", nullable = false)
    private Long clientId;
    
    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;
    
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
    
    @Column(name = "used_at")
    private OffsetDateTime usedAt;
}

