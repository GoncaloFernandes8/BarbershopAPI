// entities/RegistrationToken.java
package barbershopAPI.barbershopAPI.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name = "registration_token")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RegistrationToken {
    @Id @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String tokenHash;

    @Column(nullable = false) private String name;
    @Column(nullable = false) private String phone;
    @Column(nullable = false) private String email;
    @Column(nullable = false) private String passwordHash;

    @Column(nullable = false) private OffsetDateTime expiresAt;
    private OffsetDateTime usedAt;
    @Column(nullable = false) private OffsetDateTime createdAt;
}
