package barbershopAPI.barbershopAPI.services;

import barbershopAPI.barbershopAPI.dto.DashboardStats;
import barbershopAPI.barbershopAPI.entities.Appointment;
import barbershopAPI.barbershopAPI.enums.AppointmentStatus;
import barbershopAPI.barbershopAPI.repositories.AppointmentRepository;
import barbershopAPI.barbershopAPI.repositories.ClientRepository;
import barbershopAPI.barbershopAPI.repositories.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final AppointmentRepository appointmentRepository;
    private final ClientRepository clientRepository;
    private final ServiceRepository serviceRepository;
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    public DashboardStats calculateStatistics() {
        DashboardStats stats = new DashboardStats();
        
        // Definir período: mês atual
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Lisbon"));
        ZonedDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime endOfMonth = now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59);
        
        // Definir período: hoje
        ZonedDateTime startOfToday = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime endOfToday = now.withHour(23).withMinute(59).withSecond(59);
        
        // Definir período: esta semana
        ZonedDateTime startOfWeek = now.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime endOfWeek = now.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 7)
                .withHour(23).withMinute(59).withSecond(59);
        
        // Buscar todas as marcações do mês
        List<Appointment> monthAppointments = appointmentRepository
                .findAllByStartsAtBetween(startOfMonth.toOffsetDateTime(), endOfMonth.toOffsetDateTime());
        
        // Buscar marcações de hoje
        List<Appointment> todayAppointments = monthAppointments.stream()
                .filter(a -> !a.getStartsAt().isBefore(startOfToday.toOffsetDateTime()) 
                          && !a.getStartsAt().isAfter(endOfToday.toOffsetDateTime()))
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                .toList();
        
        // Buscar marcações da semana
        List<Appointment> weekAppointments = monthAppointments.stream()
                .filter(a -> !a.getStartsAt().isBefore(startOfWeek.toOffsetDateTime()) 
                          && !a.getStartsAt().isAfter(endOfWeek.toOffsetDateTime()))
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                .toList();
        
        // 1. Marcações de hoje
        stats.setTodayAppointments(todayAppointments.size());
        
        // 2. Total de clientes
        stats.setTotalClients((int) clientRepository.count());
        
        // 3. Receita mensal (apenas confirmadas e completadas)
        double monthlyRevenue = monthAppointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.CONFIRMED 
                          || a.getStatus() == AppointmentStatus.COMPLETED)
                .mapToDouble(a -> a.getService().getPriceCents() / 100.0)
                .sum();
        stats.setMonthlyRevenue(monthlyRevenue);
        
        // 4. Serviços ativos
        stats.setActiveServices((int) serviceRepository.findAll().stream()
                .filter(barbershopAPI.barbershopAPI.entities.Service::isActive)
                .count());
        
        // 5. Total de marcações do mês
        stats.setTotalAppointments(monthAppointments.size());
        
        // 6. Estatísticas por dia da semana
        List<DashboardStats.WeekDayStats> weekStats = calculateWeekStats(weekAppointments, startOfWeek);
        stats.setWeekStats(weekStats);
        
        // 7. Serviços mais populares
        List<DashboardStats.ServicePopularity> popularServices = calculatePopularServices(monthAppointments);
        stats.setPopularServices(popularServices);
        
        // 8. Marcações recentes de hoje (últimas 5)
        List<DashboardStats.RecentAppointment> recentAppointments = todayAppointments.stream()
                .sorted(Comparator.comparing(Appointment::getStartsAt))
                .limit(5)
                .map(a -> new DashboardStats.RecentAppointment(
                        a.getClient().getName(),
                        a.getService().getName(),
                        a.getStartsAt().toLocalTime().format(TIME_FORMATTER),
                        a.getStatus().name()
                ))
                .collect(Collectors.toList());
        stats.setRecentAppointments(recentAppointments);
        
        return stats;
    }
    
    private List<DashboardStats.WeekDayStats> calculateWeekStats(List<Appointment> weekAppointments, ZonedDateTime startOfWeek) {
        // Criar mapa de contagem por dia
        Map<Integer, Integer> appointmentsByDay = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            appointmentsByDay.put(i, 0);
        }
        
        // Contar marcações por dia
        for (Appointment appointment : weekAppointments) {
            ZonedDateTime appointmentDate = appointment.getStartsAt().atZoneSameInstant(ZoneId.of("Europe/Lisbon"));
            int dayOfWeek = appointmentDate.getDayOfWeek().getValue() - 1; // 0 = Segunda
            appointmentsByDay.merge(dayOfWeek, 1, Integer::sum);
        }
        
        // Criar lista de stats
        List<DashboardStats.WeekDayStats> weekStats = new ArrayList<>();
        String[] dayNames = {"Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom"};
        
        for (int i = 0; i < 7; i++) {
            weekStats.add(new DashboardStats.WeekDayStats(
                    dayNames[i],
                    appointmentsByDay.get(i)
            ));
        }
        
        return weekStats;
    }
    
    private List<DashboardStats.ServicePopularity> calculatePopularServices(List<Appointment> appointments) {
        // Agrupar por serviço
        Map<Long, List<Appointment>> appointmentsByService = appointments.stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                .collect(Collectors.groupingBy(a -> a.getService().getId()));
        
        // Calcular popularidade e receita
        return appointmentsByService.entrySet().stream()
                .map(entry -> {
                    List<Appointment> serviceAppointments = entry.getValue();
                    if (serviceAppointments.isEmpty()) {
                        return null;
                    }
                    
                    barbershopAPI.barbershopAPI.entities.Service service = serviceAppointments.get(0).getService();
                    long count = serviceAppointments.size();
                    double revenue = serviceAppointments.stream()
                            .filter(a -> a.getStatus() == AppointmentStatus.CONFIRMED 
                                      || a.getStatus() == AppointmentStatus.COMPLETED)
                            .mapToDouble(a -> a.getService().getPriceCents() / 100.0)
                            .sum();
                    
                    return new DashboardStats.ServicePopularity(
                            service.getName(),
                            count,
                            revenue
                    );
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(DashboardStats.ServicePopularity::getCount).reversed())
                .collect(Collectors.toList());
    }
}

