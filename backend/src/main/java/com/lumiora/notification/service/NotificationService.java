package com.lumiora.notification.service;

import java.util.List;

import org.springframework.security.core.Authentication;

import com.lumiora.notification.dto.NotificationBroadcastRequest;
import com.lumiora.notification.dto.NotificationBroadcastResponse;
import com.lumiora.notification.dto.NotificationCreateRequest;
import com.lumiora.notification.dto.NotificationResponse;

public interface NotificationService {

    NotificationResponse createNotification(
            NotificationCreateRequest request,
            Authentication authentication
    );


    NotificationBroadcastResponse broadcastNotification(
            NotificationBroadcastRequest request,
            Authentication authentication
    );


    List<NotificationResponse> getMyNotifications(
            Authentication authentication
    );


    NotificationResponse getMyNotification(
            Long notificationId,
            Authentication authentication
    );


    NotificationResponse markAsRead(
            Long notificationId,
            Authentication authentication
    );


    long markAllAsRead(
            Authentication authentication
    );


    void deactivateNotification(
            Long notificationId,
            Authentication authentication
    );
}