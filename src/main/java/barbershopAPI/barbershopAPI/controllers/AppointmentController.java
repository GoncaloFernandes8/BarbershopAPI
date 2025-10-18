package barbershopAPI.barbershopAPI.controllers;


import barbershopAPI.barbershopAPI.dto.AppointmentDTOs.AppointmentResponse;
import barbershopAPI.barbershopAPI.dto.AppointmentDTOs.CreateAppointmentRequest;
import barbershopAPI.barbershopAPI.enums.AppointmentStatus;
import barbershopAPI.barbershopAPI.entities.Appointment;
import barbershopAPI.barbershopAPI.repositories.AppointmentRepository;
import barbershopAPI.barbershopAPI.services.AppointmentService;
import barbershopAPI.barbershopAPI.services.JwtService;
import barbershopAPI.barbershopAPI.utils.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController @RequestMapping("/appointments") @RequiredArgsConstructor
public class AppointmentController {
    private final AppointmentService appointmentService;
    private final AppointmentRepository appointmentRepo;
    private final JwtService jwtService;

    @PostMapping
    public AppointmentResponse create(@Valid @RequestBody CreateAppointmentRequest req) {
        return appointmentService.create(req);
    }

    @GetMapping
    public List<AppointmentResponse> list(@RequestParam Long barberId,
                                          @RequestParam OffsetDateTime from,
                                          @RequestParam OffsetDateTime to) {
        return appointmentRepo.findAllByBarberIdAndStartsAtBetween(barberId, from, to).stream()
                .map(a -> new AppointmentResponse(a.getId(), a.getBarber().getId(), a.getService().getId(),
                        a.getClient().getId(), a.getStartsAt(), a.getEndsAt(), a.getStatus().name(), a.getNotes()))
                .toList();
    }

    @PatchMapping("/{id}/cancel")
    public AppointmentResponse cancel(@PathVariable UUID id) {
        Appointment a = appointmentRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        a.setStatus(AppointmentStatus.CANCELLED);
        a.setActive(false);
        a = appointmentRepo.save(a);
        return new AppointmentResponse(a.getId(), a.getBarber().getId(), a.getService().getId(),
                a.getClient().getId(), a.getStartsAt(), a.getEndsAt(), a.getStatus().name(), a.getNotes());
    }


    @GetMapping("/{id}")
    public AppointmentResponse get(@PathVariable UUID id) {
        Appointment a = appointmentRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        return new AppointmentResponse(a.getId(), a.getBarber().getId(), a.getService().getId(),
                a.getClient().getId(), a.getStartsAt(), a.getEndsAt(), a.getStatus().name(), a.getNotes());
    }

    @GetMapping("/my")
    public List<AppointmentResponse> getMyAppointments(@RequestHeader("Authorization") String authHeader) {
        try {
            // Validar header de autorização
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Header Authorization inválido");
            }
            
            // Extrair clientId do token JWT
            String token = authHeader.substring(7); // Remove "Bearer "
            
            // Validar token
            if (!jwtService.validateToken(token)) {
                throw new RuntimeException("Token JWT inválido ou expirado");
            }
            
            String clientIdStr = jwtService.extractUsername(token);
            Long clientId = Long.valueOf(clientIdStr);
            
            // Por enquanto, retornar lista vazia para testar se o problema é na consulta
            return List.of();
            
            // Código original comentado para debug:
            /*
            return appointmentRepo.findAllByClientIdOrderByStartsAtDesc(clientId).stream()
                    .map(a -> new AppointmentResponse(a.getId(), a.getBarber().getId(), a.getService().getId(),
                            a.getClient().getId(), a.getStartsAt(), a.getEndsAt(), a.getStatus().name(), a.getNotes()))
                    .toList();
            */
        } catch (NumberFormatException e) {
            throw new RuntimeException("Erro ao converter clientId: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar token JWT: " + e.getMessage(), e);
        }
    }

    // Endpoint temporário para debug - remover depois
    @GetMapping("/debug")
    public String debug() {
        return "Endpoint funcionando - " + new java.util.Date();
    }
}