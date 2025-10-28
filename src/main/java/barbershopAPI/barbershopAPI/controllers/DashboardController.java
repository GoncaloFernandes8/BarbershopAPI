package barbershopAPI.barbershopAPI.controllers;

import barbershopAPI.barbershopAPI.dto.DashboardStats;
import barbershopAPI.barbershopAPI.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;
    
    @GetMapping("/statistics")
    public DashboardStats getStatistics() {
        return dashboardService.calculateStatistics();
    }
}

