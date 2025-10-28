package barbershopAPI.barbershopAPI.controllers;


import barbershopAPI.barbershopAPI.dto.AppointmentDTOs.AppointmentResponse;
import barbershopAPI.barbershopAPI.dto.AppointmentDTOs.CreateAppointmentRequest;
import barbershopAPI.barbershopAPI.dto.AppointmentDTOs.UpdateAppointmentRequest;
import barbershopAPI.barbershopAPI.dto.AppointmentDTOs.UpdateStatusRequest;
import barbershopAPI.barbershopAPI.enums.AppointmentStatus;
import barbershopAPI.barbershopAPI.entities.Appointment;
import barbershopAPI.barbershopAPI.repositories.AppointmentRepository;
import barbershopAPI.barbershopAPI.repositories.ClientRepository;
import barbershopAPI.barbershopAPI.services.AppointmentService;
import barbershopAPI.barbershopAPI.services.JwtService;
import barbershopAPI.barbershopAPI.services.NotificationService;
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
    private final ClientRepository clientRepo;
    private final NotificationService notificationService;

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
        
        // Create notification for cancelled appointment
        try {
            String timeStr = a.getStartsAt().toLocalTime().toString();
            notificationService.notifyAppointmentCancelled(a.getClient().getName(), timeStr);
        } catch (Exception ex) {
            // Log error but don't fail the request
            System.err.println("Falha ao criar notificação para cancelamento: " + ex.getMessage());
        }
        
        return new AppointmentResponse(a.getId(), a.getBarber().getId(), a.getService().getId(),
                a.getClient().getId(), a.getStartsAt(), a.getEndsAt(), a.getStatus().name(), a.getNotes());
    }

    @PatchMapping("/{id}/status")
    public AppointmentResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateStatusRequest req) {
        Appointment a = appointmentRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        
        try {
            AppointmentStatus newStatus = AppointmentStatus.valueOf(req.status().toUpperCase());
            a.setStatus(newStatus);
            
            // Se cancelar, desativar
            if (newStatus == AppointmentStatus.CANCELLED) {
                a.setActive(false);
            }
            
            a = appointmentRepo.save(a);
            
            // Create notification based on status change
            try {
                String timeStr = a.getStartsAt().toLocalTime().toString();
                if (newStatus == AppointmentStatus.SCHEDULED) {
                    notificationService.notifyAppointmentConfirmed(a.getClient().getName(), timeStr);
                } else if (newStatus == AppointmentStatus.CANCELLED) {
                    notificationService.notifyAppointmentCancelled(a.getClient().getName(), timeStr);
                }
            } catch (Exception ex) {
                // Log error but don't fail the request
                System.err.println("Falha ao criar notificação para mudança de status: " + ex.getMessage());
            }
            
            return new AppointmentResponse(a.getId(), a.getBarber().getId(), a.getService().getId(),
                    a.getClient().getId(), a.getStartsAt(), a.getEndsAt(), a.getStatus().name(), a.getNotes());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status. Valid values: SCHEDULED, CANCELLED, COMPLETED, NO_SHOW");
        }
    }


    @GetMapping("/{id}")
    public AppointmentResponse get(@PathVariable UUID id) {
        Appointment a = appointmentRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        return new AppointmentResponse(a.getId(), a.getBarber().getId(), a.getService().getId(),
                a.getClient().getId(), a.getStartsAt(), a.getEndsAt(), a.getStatus().name(), a.getNotes());
    }

    @PutMapping("/{id}")
    public AppointmentResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateAppointmentRequest req) {
        return appointmentService.update(id, req);
    }

    @GetMapping("/my")
    public List<AppointmentResponse> getMyAppointments(@RequestHeader("Authorization") String authHeader) {
        try {
            System.out.println("=== ENDPOINT /my CHAMADO ===");
            System.out.println("Header Authorization: " + authHeader);
            
            // Validar header de autorização
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.out.println("ERRO: Header Authorization inválido");
                throw new org.springframework.security.authentication.BadCredentialsException("Header Authorization inválido");
            }
            
            // Extrair clientId do token JWT
            String token = authHeader.substring(7); // Remove "Bearer "
            System.out.println("Token extraído: " + token.substring(0, Math.min(50, token.length())) + "...");
            
            // Validar token
            boolean isValid = jwtService.validateToken(token);
            System.out.println("Token válido: " + isValid);
            
            if (!isValid) {
                System.out.println("ERRO: Token JWT inválido ou expirado");
                throw new org.springframework.security.authentication.BadCredentialsException("Token JWT inválido ou expirado");
            }
            
            String email = jwtService.extractUsername(token);
            System.out.println("Email extraído: " + email);
            
            // Buscar cliente pelo email
            var client = clientRepo.findByEmailIgnoreCase(email).orElse(null);
            if (client == null) {
                System.out.println("ERRO: Cliente não encontrado para email: " + email);
                throw new org.springframework.security.authentication.BadCredentialsException("Cliente não encontrado");
            }
            
            Long clientId = client.getId();
            System.out.println("ClientId encontrado: " + clientId);
            
            List<Appointment> appointments = appointmentRepo.findAllByClientIdOrderByStartsAtDesc(clientId);
            System.out.println("Appointments encontrados: " + appointments.size());
            
            List<AppointmentResponse> result = appointments.stream()
                    .map(a -> new AppointmentResponse(a.getId(), a.getBarber().getId(), a.getService().getId(),
                            a.getClient().getId(), a.getStartsAt(), a.getEndsAt(), a.getStatus().name(), a.getNotes()))
                    .toList();
            
            System.out.println("Resultado retornado: " + result.size() + " appointments");
            return result;
                    
        } catch (NumberFormatException e) {
            System.out.println("ERRO NumberFormatException: " + e.getMessage());
            throw new org.springframework.security.authentication.BadCredentialsException("Erro ao converter clientId: " + e.getMessage());
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            System.out.println("ERRO BadCredentialsException: " + e.getMessage());
            // Re-throw BadCredentialsException as is
            throw e;
        } catch (Exception e) {
            System.out.println("ERRO Exception: " + e.getMessage());
            e.printStackTrace();
            // Se for token expirado, retornar 401 em vez de 500
            if (e.getMessage().contains("JWT expired")) {
                throw new org.springframework.security.authentication.BadCredentialsException("Token expirado");
            }
            throw new RuntimeException("Erro ao processar token JWT: " + e.getMessage(), e);
        }
    }

    // Endpoint temporário para debug - remover depois
    @GetMapping("/debug")
    public String debug() {
        return "Endpoint funcionando - " + new java.util.Date();
    }

    // Endpoint para testar dados mockados
    @GetMapping("/test")
    public List<AppointmentResponse> testData() {
        return List.of(
            new AppointmentResponse(
                java.util.UUID.fromString("123e4567-e89b-12d3-a456-426614174000"), // UUID fixo válido
                1L, // barberId
                1L, // serviceId
                1L, // clientId
                java.time.OffsetDateTime.now(),
                java.time.OffsetDateTime.now().plusHours(1),
                "CONFIRMED",
                "Teste de dados"
            )
        );
    }
}