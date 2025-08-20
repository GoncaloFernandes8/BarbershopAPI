package barbershopAPI.barbershopAPI.controllers;

import barbershopAPI.barbershopAPI.dto.BarberDTOs.BarberCreateRequest;
import barbershopAPI.barbershopAPI.dto.BarberDTOs.BarberResponse;
import barbershopAPI.barbershopAPI.dto.BarberDTOs.BarberUpdateRequest;
import barbershopAPI.barbershopAPI.entities.Barber;
import barbershopAPI.barbershopAPI.repositories.BarberRepository;
import barbershopAPI.barbershopAPI.utils.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/barbers")
@RequiredArgsConstructor
public class BarberController {
    private final BarberRepository repo;

    @PostMapping
    public BarberResponse create(@Valid @RequestBody BarberCreateRequest req) {
        Barber b = Barber.builder().name(req.name()).isActive(true).build();
        b = repo.save(b);
        return new BarberResponse(b.getId(), b.getName(), b.isActive(), b.getCreatedAt());
    }

    @GetMapping
    public List<BarberResponse> list() {
        return repo.findAll().stream()
                .map(b -> new BarberResponse(b.getId(), b.getName(), b.isActive(), b.getCreatedAt()))
                .toList();
    }

    @GetMapping("/{id}")
    public BarberResponse get(@PathVariable Long id) {
        Barber b = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Barber not found"));
        return new BarberResponse(b.getId(), b.getName(), b.isActive(), b.getCreatedAt());
    }

    @PutMapping("/{id}")
    public BarberResponse update(@PathVariable Long id, @Valid @RequestBody BarberUpdateRequest req) {
        Barber b = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Barber not found"));
        b.setName(req.name());
        if (req.active() != null) b.setActive(req.active());
        b = repo.save(b);
        return new BarberResponse(b.getId(), b.getName(), b.isActive(), b.getCreatedAt());
    }

    @DeleteMapping("/{id}")
    public void softDelete(@PathVariable Long id) {
        Barber b = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Barber not found"));
        b.setActive(false);
        repo.save(b);
    }
}
