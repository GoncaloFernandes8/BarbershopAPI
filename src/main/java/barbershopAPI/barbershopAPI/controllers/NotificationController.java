package barbershopAPI.barbershopAPI.controllers;

import barbershopAPI.barbershopAPI.entities.Notification;
import barbershopAPI.barbershopAPI.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    // Get all notifications
    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        List<Notification> notifications = notificationService.getAllNotifications();
        return ResponseEntity.ok(notifications);
    }
    
    // Get notifications with pagination
    @GetMapping("/page")
    public ResponseEntity<Page<Notification>> getNotificationsWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Notification> notifications = notificationService.getNotificationsWithPagination(page, size);
        return ResponseEntity.ok(notifications);
    }
    
    // Get unread notifications
    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications() {
        List<Notification> notifications = notificationService.getUnreadNotifications();
        return ResponseEntity.ok(notifications);
    }
    
    // Get unread count
    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount() {
        long count = notificationService.getUnreadCount();
        return ResponseEntity.ok(count);
    }
    
    // Get notification by ID
    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotificationById(@PathVariable Long id) {
        Optional<Notification> notification = notificationService.getNotificationById(id);
        return notification.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Mark notification as read
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        boolean success = notificationService.markAsRead(id);
        return success ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
    
    // Mark all notifications as read
    @PutMapping("/read-all")
    public ResponseEntity<Integer> markAllAsRead() {
        int updated = notificationService.markAllAsRead();
        return ResponseEntity.ok(updated);
    }
    
    // Delete notification
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        boolean success = notificationService.deleteNotification(id);
        return success ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
    
    // Delete old notifications
    @DeleteMapping("/cleanup")
    public ResponseEntity<Integer> deleteOldNotifications() {
        int deleted = notificationService.deleteOldNotifications();
        return ResponseEntity.ok(deleted);
    }
    
    // Create notification (for testing or admin purposes)
    @PostMapping
    public ResponseEntity<Notification> createNotification(@RequestBody CreateNotificationRequest request) {
        Notification notification = notificationService.createNotification(
            request.getType(),
            request.getTitle(),
            request.getMessage(),
            request.getIcon(),
            request.getActionUrl()
        );
        return ResponseEntity.ok(notification);
    }
    
    // DTO for creating notifications
    public static class CreateNotificationRequest {
        private Notification.NotificationType type;
        private String title;
        private String message;
        private String icon;
        private String actionUrl;
        
        // Getters and Setters
        public Notification.NotificationType getType() {
            return type;
        }
        
        public void setType(Notification.NotificationType type) {
            this.type = type;
        }
        
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public String getIcon() {
            return icon;
        }
        
        public void setIcon(String icon) {
            this.icon = icon;
        }
        
        public String getActionUrl() {
            return actionUrl;
        }
        
        public void setActionUrl(String actionUrl) {
            this.actionUrl = actionUrl;
        }
    }
}
