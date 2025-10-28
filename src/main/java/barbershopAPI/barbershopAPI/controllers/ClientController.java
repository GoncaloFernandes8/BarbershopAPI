package barbershopAPI.barbershopAPI.controllers;

import barbershopAPI.barbershopAPI.dto.ClientDTOs.ClientCreateRequest;
import barbershopAPI.barbershopAPI.dto.ClientDTOs.ClientResponse;
import barbershopAPI.barbershopAPI.dto.ClientDTOs.ClientUpdateRequest;
import barbershopAPI.barbershopAPI.entities.Client;
import barbershopAPI.barbershopAPI.repositories.ClientRepository;
import barbershopAPI.barbershopAPI.services.NotificationService;
import barbershopAPI.barbershopAPI.utils.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {
    private final ClientRepository repo;
    private final NotificationService notificationService;

    @PostMapping
    public ClientResponse create(@Valid @RequestBody ClientCreateRequest req) {
        Client c = Client.builder().name(req.name()).phone(req.phone()).email(req.email()).password(req.password()).build();
        c = repo.save(c);
        
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
}
