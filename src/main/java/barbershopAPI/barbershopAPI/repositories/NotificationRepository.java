package barbershopAPI.barbershopAPI.repositories;

import barbershopAPI.barbershopAPI.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Find all notifications ordered by creation date (newest first)
    List<Notification> findAllByOrderByCreatedAtDesc();
    
    // Find unread notifications
    List<Notification> findByReadStatusFalseOrderByCreatedAtDesc();
    
    // Count unread notifications
    long countByReadStatusFalse();
    
    // Find notifications by type
    List<Notification> findByTypeOrderByCreatedAtDesc(Notification.NotificationType type);
    
    // Find notifications created after a specific date
    List<Notification> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime date);
    
    // Mark all notifications as read
    @Modifying
    @Query("UPDATE Notification n SET n.readStatus = true, n.updatedAt = CURRENT_TIMESTAMP WHERE n.readStatus = false")
    int markAllAsRead();
    
    // Mark specific notification as read
    @Modifying
    @Query("UPDATE Notification n SET n.readStatus = true, n.updatedAt = CURRENT_TIMESTAMP WHERE n.id = :id")
    int markAsRead(@Param("id") Long id);
    
    // Delete old notifications (older than specified days)
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
    int deleteOldNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Find notifications with pagination
    @Query("SELECT n FROM Notification n ORDER BY n.createdAt DESC")
    List<Notification> findNotificationsWithPagination(org.springframework.data.domain.Pageable pageable);
}
