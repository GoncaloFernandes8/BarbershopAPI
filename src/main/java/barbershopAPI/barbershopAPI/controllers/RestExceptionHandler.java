package barbershopAPI.barbershopAPI.controllers;

import barbershopAPI.barbershopAPI.utils.ResourceNotFoundException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import barbershopAPI.barbershopAPI.services.AppointmentService.SlotConflictException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String,Object>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error","NOT_FOUND","message", ex.getMessage()));
    }
    @ExceptionHandler(SlotConflictException.class)
    public ResponseEntity<Map<String,Object>> handleSlot(SlotConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error","CONFLICT","message", ex.getMessage()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body(Map.of("error","BAD_REQUEST","message","Validation failed"));
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String,Object>> handleDI(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error","CONFLICT","message","Constraint violation"));
    }
    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<Map<String,Object>> handleInvalidFormat(InvalidFormatException ex) {
        return ResponseEntity.badRequest().body(Map.of("error","BAD_REQUEST","message","Invalid data format"));
    }
}
