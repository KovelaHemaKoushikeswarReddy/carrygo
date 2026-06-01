package com.cts.mrfp.carrygo.controller;

import com.cts.mrfp.carrygo.model.Notifications;
import com.cts.mrfp.carrygo.dto.NotificationsDTO;
import com.cts.mrfp.carrygo.service.NotificationsService;
import com.cts.mrfp.carrygo.util.DTOConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

// Endpoints for the in-app notification bell:
// listing a user's notifications, creating new ones, and marking them as read.
@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationsController {

    @Autowired
    private NotificationsService notificationsService;

    // GET /api/notifications/user/{userId} — all notifications for one user.
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationsDTO>> getUserNotifications(@PathVariable Integer userId) {
        List<NotificationsDTO> dtos = notificationsService.getUserNotifications(userId).stream()
            .map(DTOConverter::convertNotificationsToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // POST /api/notifications — save a new notification (used for manual / test sends).
    @PostMapping
    public ResponseEntity<NotificationsDTO> sendNotification(@RequestBody NotificationsDTO notificationDTO) {
        Notifications notification = new Notifications();
        notification.setType(notificationDTO.getType());
        notification.setMessage(notificationDTO.getMessage());
        notification.setIsRead(Boolean.TRUE.equals(notificationDTO.getIsRead()));

        Notifications sent = notificationsService.sendNotification(notification);
        return ResponseEntity.ok(DTOConverter.convertNotificationsToDTO(sent));
    }

    // PATCH /api/notifications/{id}/read — flip a single notification to "read".
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Integer id) {
        notificationsService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }
}
