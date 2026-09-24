package com.lumiora.notification.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lumiora.dto.response.ApiResponse;

import com.lumiora.notification.dto.NotificationBroadcastRequest;
import com.lumiora.notification.dto.NotificationBroadcastResponse;
import com.lumiora.notification.dto.NotificationCreateRequest;
import com.lumiora.notification.dto.NotificationResponse;

import com.lumiora.notification.service.NotificationService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;


    // =========================================================
    // CREATE NOTIFICATION FOR ONE USER
    // =========================================================

    @PostMapping
    public ResponseEntity<ApiResponse<NotificationResponse>>
            createNotification(
                    @Valid @RequestBody
                    NotificationCreateRequest request,
                    Authentication authentication
            ) {

        NotificationResponse response =
                notificationService.createNotification(
                        request,
                        authentication
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Notification created successfully",
                                response
                        )
                );
    }


    // =========================================================
    // BROADCAST
    // =========================================================

    @PostMapping("/broadcast")
    public ResponseEntity<
            ApiResponse<NotificationBroadcastResponse>>
            broadcastNotification(
                    @Valid @RequestBody
                    NotificationBroadcastRequest request,
                    Authentication authentication
            ) {

        NotificationBroadcastResponse response =
                notificationService.broadcastNotification(
                        request,
                        authentication
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Notification broadcast successfully",
                                response
                        )
                );
    }


    // =========================================================
    // GET MY NOTIFICATIONS
    // =========================================================

    @GetMapping
    public ResponseEntity<
            ApiResponse<List<NotificationResponse>>>
            getMyNotifications(
                    Authentication authentication
            ) {

        List<NotificationResponse> notifications =
                notificationService.getMyNotifications(
                        authentication
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Notifications fetched successfully",
                        notifications
                )
        );
    }


    // =========================================================
    // UNREAD COUNT
    //
    // Kept in controller/service design for future extension.
    // Currently count is derived from getMyNotifications.
    // =========================================================

    @GetMapping("/unread-count")
    public ResponseEntity<
            ApiResponse<Long>>
            getUnreadCount(
                    Authentication authentication
            ) {

        List<NotificationResponse> notifications =
                notificationService.getMyNotifications(
                        authentication
                );


        long unreadCount =
                notifications.stream()
                        .filter(notification ->
                                !notification.isRead()
                        )
                        .count();


        return ResponseEntity.ok(
                ApiResponse.success(
                        "Unread notification count fetched successfully",
                        unreadCount
                )
        );
    }


    // =========================================================
    // GET MY NOTIFICATION
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<
            ApiResponse<NotificationResponse>>
            getMyNotification(
                    @PathVariable Long id,
                    Authentication authentication
            ) {

        NotificationResponse response =
                notificationService.getMyNotification(
                        id,
                        authentication
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Notification fetched successfully",
                        response
                )
        );
    }


    // =========================================================
    // MARK ONE AS READ
    // =========================================================

    @PutMapping("/{id}/read")
    public ResponseEntity<
            ApiResponse<NotificationResponse>>
            markAsRead(
                    @PathVariable Long id,
                    Authentication authentication
            ) {

        NotificationResponse response =
                notificationService.markAsRead(
                        id,
                        authentication
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Notification marked as read",
                        response
                )
        );
    }


    // =========================================================
    // MARK ALL AS READ
    // =========================================================

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Long>>
            markAllAsRead(
                    Authentication authentication
            ) {

        long count =
                notificationService.markAllAsRead(
                        authentication
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        count
                                + " notification(s) marked as read",
                        count
                )
        );
    }


    // =========================================================
    // DEACTIVATE
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>>
            deactivateNotification(
                    @PathVariable Long id,
                    Authentication authentication
            ) {

        notificationService.deactivateNotification(
                id,
                authentication
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Notification deactivated successfully",
                        null
                )
        );
    }
}