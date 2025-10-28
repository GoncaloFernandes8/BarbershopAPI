package barbershopAPI.barbershopAPI.services;

import barbershopAPI.barbershopAPI.entities.Notification;
import barbershopAPI.barbershopAPI.repositories.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    // Create a new notification
    public Notification createNotification(Notification.NotificationType type, String title, String message, String icon) {
        Notification notification = new Notification(type, title, message, icon);
        return notificationRepository.save(notification);
    }
    
    // Create a new notification with action URL
    public Notification createNotification(Notification.NotificationType type, String title, String message, String icon, String actionUrl) {
        Notification notification = new Notification(type, title, message, icon, actionUrl);
        return notificationRepository.save(notification);
    }
    
    // Get all notifications
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc();
    }
    
    // Get notifications with pagination
    public Page<Notification> getNotificationsWithPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return notificationRepository.findAll(pageable);
    }
    
    // Get unread notifications
    public List<Notification> getUnreadNotifications() {
        return notificationRepository.findByReadStatusFalseOrderByCreatedAtDesc();
    }
    
    // Get unread count
    public long getUnreadCount() {
        return notificationRepository.countByReadStatusFalse();
    }
    
    // Get notification by ID
    public Optional<Notification> getNotificationById(Long id) {
        return notificationRepository.findById(id);
    }
    
    // Mark notification as read
    public boolean markAsRead(Long id) {
        int updated = notificationRepository.markAsRead(id);
        return updated > 0;
    }
    
    // Mark all notifications as read
    public int markAllAsRead() {
        return notificationRepository.markAllAsRead();
    }
    
    // Delete notification
    public boolean deleteNotification(Long id) {
        if (notificationRepository.existsById(id)) {
            notificationRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    // Delete old notifications (older than 30 days)
    public int deleteOldNotifications() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        return notificationRepository.deleteOldNotifications(cutoffDate);
    }
    
    // Convenience methods for specific notification types
    
    // Notify new client
    public Notification notifyNewClient(String clientName) {
        return createNotification(
            Notification.NotificationType.CLIENT,
            "Novo Cliente",
            clientName + " foi cadastrado no sistema",
            "person_add"
        );
    }
    
    // Notify new appointment
    public Notification notifyNewAppointment(String clientName, String barberName, String time) {
        return createNotification(
            Notification.NotificationType.APPOINTMENT,
            "Nova Marcação",
            clientName + " agendou uma marcação com " + barberName + " às " + time,
            "event"
        );
    }
    
    // Notify appointment cancelled
    public Notification notifyAppointmentCancelled(String clientName, String time) {
        return createNotification(
            Notification.NotificationType.APPOINTMENT,
            "Marcação Cancelada",
            "A marcação de " + clientName + " às " + time + " foi cancelada",
            "event_busy"
        );
    }
    
    // Notify appointment confirmed
    public Notification notifyAppointmentConfirmed(String clientName, String time) {
        return createNotification(
            Notification.NotificationType.APPOINTMENT,
            "Marcação Confirmada",
            clientName + " confirmou a marcação às " + time,
            "event_available"
        );
    }
    
    // Notify new service
    public Notification notifyNewService(String serviceName) {
        return createNotification(
            Notification.NotificationType.SERVICE,
            "Novo Serviço",
            serviceName + " foi adicionado aos serviços",
            "build"
        );
    }
    
    // Notify service updated
    public Notification notifyServiceUpdated(String serviceName) {
        return createNotification(
            Notification.NotificationType.SERVICE,
            "Serviço Atualizado",
            serviceName + " foi atualizado",
            "edit"
        );
    }
    
    // Notify system message
    public Notification notifySystemMessage(String title, String message) {
        return createNotification(
            Notification.NotificationType.SYSTEM,
            title,
            message,
            "info"
        );
    }
}
