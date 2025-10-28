package barbershopAPI.barbershopAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    private Integer todayAppointments;
    private Integer totalClients;
    private Double monthlyRevenue; // em euros
    private Integer activeServices;
    private Integer totalAppointments;
    private List<WeekDayStats> weekStats;
    private List<ServicePopularity> popularServices;
    private List<RecentAppointment> recentAppointments;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WeekDayStats {
        private String day; // Nome do dia (Seg, Ter, etc)
        private Integer appointments;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ServicePopularity {
        private String serviceName;
        private Long count;
        private Double revenue; // em euros
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecentAppointment {
        private String clientName;
        private String serviceName;
        private String time; // HH:mm
        private String status;
    }
}

