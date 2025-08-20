package barbershopAPI.barbershopAPI.dto.TimeOffDTOs;

import java.time.OffsetDateTime;


public record TimeOffResponse(Long id, Long barberId, OffsetDateTime startsAt, OffsetDateTime endsAt, String reason) {}
