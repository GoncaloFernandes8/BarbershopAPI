package barbershopAPI.barbershopAPI.controllers;

import barbershopAPI.barbershopAPI.dto.ServiceDTOs.ServiceCreateRequest;
import barbershopAPI.barbershopAPI.dto.ServiceDTOs.ServiceResponse;
import barbershopAPI.barbershopAPI.dto.ServiceDTOs.ServiceUpdateRequest;
import barbershopAPI.barbershopAPI.entities.ServiceEntity;
import barbershopAPI.barbershopAPI.repositories.ServiceRepository;
import barbershopAPI.barbershopAPI.utils.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
public class ServiceController {
    private final ServiceRepository repo;

    @PostMapping
    public ServiceResponse create(@Valid @RequestBody ServiceCreateRequest req) {
        ServiceEntity s = ServiceEntity.builder()
                .name(req.name())
                .durationMin(req.durationMin())
                .bufferAfterMin(req.bufferAfterMin())
                .priceCents(req.priceCents())
                .isActive(req.active() == null ? true : req.active())
                .build();
        s = repo.save(s);
        return new ServiceResponse(s.getId(), s.getName(), s.getDurationMin(), s.getBufferAfterMin(), s.getPriceCents(), s.isActive());
    }

    @GetMapping
    public List<ServiceResponse> list() {
        return repo.findAll().stream()
                .map(s -> new ServiceResponse(s.getId(), s.getName(), s.getDurationMin(), s.getBufferAfterMin(), s.getPriceCents(), s.isActive()))
                .toList();
    }

    @GetMapping("/{id}")
    public ServiceResponse get(@PathVariable Long id) {
        var s = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Service not found"));
        return new ServiceResponse(s.getId(), s.getName(), s.getDurationMin(), s.getBufferAfterMin(), s.getPriceCents(), s.isActive());
    }

    @PutMapping("/{id}")
    public ServiceResponse update(@PathVariable Long id, @Valid @RequestBody ServiceUpdateRequest req) {
        var s = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Service not found"));
        s.setName(req.name());
        s.setDurationMin(req.durationMin());
        s.setBufferAfterMin(req.bufferAfterMin());
        s.setPriceCents(req.priceCents());
        if (req.active() != null) s.setActive(req.active());
        s = repo.save(s);
        return new ServiceResponse(s.getId(), s.getName(), s.getDurationMin(), s.getBufferAfterMin(), s.getPriceCents(), s.isActive());
    }

    @DeleteMapping("/{id}")
    public void softDelete(@PathVariable Long id) {
        var s = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Service not found"));
        s.setActive(false);
        repo.save(s);
    }
    
    @DeleteMapping("/{id}/permanent")
    public void hardDelete(@PathVariable Long id) {
        var s = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Service not found"));
        repo.delete(s);
    }
}
