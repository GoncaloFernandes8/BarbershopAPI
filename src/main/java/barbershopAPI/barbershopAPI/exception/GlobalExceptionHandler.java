package barbershopAPI.barbershopAPI.exception;

import barbershopAPI.barbershopAPI.services.AppointmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppointmentService.SlotConflictException.class)
    public ResponseEntity<Map<String, Object>> handleSlotConflict(AppointmentService.SlotConflictException ex) {
        log.warn("Conflito de horário: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Slot Conflict");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("type", "SLOT_CONFLICT");
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(AppointmentService.TimeOffConflictException.class)
    public ResponseEntity<Map<String, Object>> handleTimeOffConflict(AppointmentService.TimeOffConflictException ex) {
        log.warn("Conflito com folga: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Time-Off Conflict");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("type", "TIME_OFF_CONFLICT");
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}

