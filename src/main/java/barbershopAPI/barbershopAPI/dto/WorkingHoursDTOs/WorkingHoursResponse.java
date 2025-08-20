package barbershopAPI.barbershopAPI.dto.WorkingHoursDTOs;

public record WorkingHoursResponse(Long id, Long barberId, Integer dayOfWeek, String startTime, String endTime) {}
