package barbershopAPI.barbershopAPI.services;

import barbershopAPI.barbershopAPI.entities.Appointment;
import barbershopAPI.barbershopAPI.enums.AppointmentStatus;
import barbershopAPI.barbershopAPI.repositories.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentReminderScheduler {

    private final AppointmentRepository appointmentRepository;
    private final EmailService emailService;
    
    // Set para rastrear IDs de marcações que já receberam lembrete
    private final Set<UUID> remindersSent = new HashSet<>();
    
    private static final ZoneId LISBON_ZONE = ZoneId.of("Europe/Lisbon");
    
    /**
     * Executa a cada 5 minutos para verificar marcações que precisam de lembrete
     * Cron: segundo minuto hora dia mês dia-da-semana
     */
    @Scheduled(cron = "0 */5 * * * *") // A cada 5 minutos
    public void checkAndSendReminders() {
        try {
            OffsetDateTime now = OffsetDateTime.now(LISBON_ZONE);
            
            // Janela de tempo: entre 55 minutos e 1 hora e 5 minutos a partir de agora
            // Isto garante que apanhamos marcações mesmo com pequenos atrasos do scheduler
            OffsetDateTime startWindow = now.plusMinutes(55);
            OffsetDateTime endWindow = now.plusMinutes(65);
            
            log.debug("Verificando marcações entre {} e {}", startWindow, endWindow);
            
            // Buscar marcações ativas que começam nesta janela de tempo
            List<Appointment> upcomingAppointments = appointmentRepository
                .findAllByIsActiveTrueAndStartsAtBetween(startWindow, endWindow);
            
            log.info("Encontradas {} marcação(ões) para lembrete", upcomingAppointments.size());
            
            for (Appointment appointment : upcomingAppointments) {
                // Verificar se já enviamos lembrete para esta marcação
                if (remindersSent.contains(appointment.getId())) {
                    log.debug("Lembrete já enviado para marcação {}", appointment.getId());
                    continue;
                }
                
                // Verificar se a marcação está em estado válido para lembrete
                // Só envia para marcações ativas (não canceladas, não completadas)
                if (appointment.getStatus() == AppointmentStatus.CANCELLED || 
                    appointment.getStatus() == AppointmentStatus.COMPLETED ||
                    appointment.getStatus() == AppointmentStatus.NO_SHOW) {
                    log.debug("Marcação {} em estado {} - não enviando lembrete", 
                             appointment.getId(), appointment.getStatus());
                    continue;
                }
                
                // Verificar se o cliente tem email
                if (appointment.getClient() == null || 
                    appointment.getClient().getEmail() == null || 
                    appointment.getClient().getEmail().isBlank()) {
                    log.warn("Marcação {} não tem email válido do cliente", appointment.getId());
                    continue;
                }
                
                try {
                    // Enviar lembrete
                    emailService.sendAppointmentReminder(appointment);
                    
                    // Marcar como enviado
                    remindersSent.add(appointment.getId());
                    
                    log.info("✅ Lembrete enviado com sucesso para {} (Marcação: {})", 
                             appointment.getClient().getEmail(), 
                             appointment.getId());
                             
                } catch (Exception e) {
                    log.error("❌ Erro ao enviar lembrete para marcação {}: {}", 
                             appointment.getId(), 
                             e.getMessage(), e);
                }
            }
            
            // Limpar cache de lembretes enviados (marcações de mais de 2 horas atrás)
            cleanupOldReminders(now);
            
        } catch (Exception e) {
            log.error("Erro no scheduler de lembretes: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Remove do cache marcações antigas para evitar crescimento infinito
     */
    private void cleanupOldReminders(OffsetDateTime now) {
        OffsetDateTime twoHoursAgo = now.minusHours(2);
        
        // Buscar IDs de marcações antigas que ainda estão no cache
        List<UUID> oldAppointmentIds = appointmentRepository
            .findAllByStartsAtBefore(twoHoursAgo)
            .stream()
            .map(Appointment::getId)
            .toList();
        
        // Remover do cache
        int removed = 0;
        for (UUID id : oldAppointmentIds) {
            if (remindersSent.remove(id)) {
                removed++;
            }
        }
        
        if (removed > 0) {
            log.debug("Removidas {} marcações antigas do cache de lembretes", removed);
        }
    }
}

