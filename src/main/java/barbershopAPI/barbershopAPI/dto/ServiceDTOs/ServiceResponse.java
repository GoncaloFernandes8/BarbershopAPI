package barbershopAPI.barbershopAPI.dto.ServiceDTOs;

public record ServiceResponse(Long id, String name, Integer durationMin, Integer bufferAfterMin, Integer priceCents, boolean active) {}

