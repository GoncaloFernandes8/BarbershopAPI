package barbershopAPI.barbershopAPI.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity @Table(name = "service")
public class ServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String name;

    /** duração do serviço em minutos (ex.: 30) */
    @Column(name = "duration_min", nullable = false)
    private Integer durationMin;

    /** buffer pós-serviço em minutos (ex.: 0..15) */
    @Builder.Default
    @Column(name = "buffer_after_min", nullable = false)
    private Integer bufferAfterMin = 0;

    /** preço opcional em cêntimos */
    @Column(name = "price_cents")
    private Integer priceCents;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
