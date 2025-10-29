package barbershopAPI.barbershopAPI.controllers;

import barbershopAPI.barbershopAPI.dto.ClientDTOs.ClientCreateRequest;
import barbershopAPI.barbershopAPI.dto.ClientDTOs.ClientResponse;
import barbershopAPI.barbershopAPI.dto.ClientDTOs.ClientUpdateRequest;
import barbershopAPI.barbershopAPI.entities.Client;
import barbershopAPI.barbershopAPI.repositories.ClientRepository;
import barbershopAPI.barbershopAPI.services.NotificationService;
import barbershopAPI.barbershopAPI.services.SetPasswordService;
import barbershopAPI.barbershopAPI.utils.ResourceNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {
    private final ClientRepository repo;
    private final NotificationService notificationService;
    private final SetPasswordService setPasswordService;

    @PostMapping
    public ClientResponse create(@Valid @RequestBody ClientCreateRequest req) {
        // Criar cliente SEM senha (será definida via email)
        Client c = Client.builder()
                .name(req.name())
                .phone(req.phone())
                .email(req.email())
                .password(null) // Senha será definida depois
                .build();
        c = repo.save(c);
        
        // Enviar email para cliente definir senha
        try {
            setPasswordService.sendSetPasswordEmail(c.getId());
        } catch (Exception ex) {
            // Log mas não falha a criação do cliente
            System.err.println("Erro ao enviar email de definição de senha: " + ex.getMessage());
        }
        
        // Create notification for new client
        notificationService.notifyNewClient(c.getName());
        
        return new ClientResponse(c.getId(), c.getName(), c.getPhone());
    }

    @GetMapping
    public List<ClientResponse> list() {
        return repo.findAll().stream().map(c -> new ClientResponse(c.getId(), c.getName(), c.getPhone())).toList();
    }

    @GetMapping("/{id}")
    public ClientResponse get(@PathVariable Long id) {
        var c = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Client not found"));
        return new ClientResponse(c.getId(), c.getName(), c.getPhone());
    }

    @PutMapping("/{id}")
    public ClientResponse update(@PathVariable Long id, @Valid @RequestBody ClientUpdateRequest req) {
        var c = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Client not found"));
        c.setName(req.name());
        c.setPhone(req.phone());
        c = repo.save(c);
        return new ClientResponse(c.getId(), c.getName(), c.getPhone());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }

    // Endpoint para definir senha via token
    @PostMapping("/set-password")
    public ResponseEntity<?> setPassword(@Valid @RequestBody SetPasswordRequest req) {
        try {
            setPasswordService.setPassword(req.token(), req.password());
            return ResponseEntity.ok().body(new SetPasswordResponse(true, "Senha definida com sucesso"));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(new SetPasswordResponse(false, ex.getMessage()));
        }
    }

    // Endpoint para reenviar email de definição de senha
    @PostMapping("/{id}/resend-set-password")
    public ResponseEntity<?> resendSetPasswordEmail(@PathVariable Long id) {
        try {
            setPasswordService.sendSetPasswordEmail(id);
            return ResponseEntity.ok().body(new ResendResponse(true, "Email enviado com sucesso"));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(new ResendResponse(false, ex.getMessage()));
        }
    }

    // DTOs
    public record SetPasswordRequest(@NotBlank String token, @NotBlank String password) {}
    public record SetPasswordResponse(boolean success, String message) {}
    public record ResendResponse(boolean success, String message) {}
}
