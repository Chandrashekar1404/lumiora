package com.lumiora.notification.dto;

import java.time.LocalDateTime;

import com.lumiora.notification.entity.NotificationType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NotificationResponse {

    private Long id;

    private String title;

    private String message;

    private NotificationType type;

    private boolean read;

    private LocalDateTime readAt;

    private boolean active;

    private LocalDateTime createdAt;

    private Long organizationId;

    private Long recipientId;

    private String recipientName;

    private Long senderId;

    private String senderName;


    public NotificationResponse(
            Long id,
            String title,
            String message,
            NotificationType type,
            boolean read,
            LocalDateTime readAt,
            boolean active,
            LocalDateTime createdAt,
            Long organizationId,
            Long recipientId,
            String recipientName,
            Long senderId,
            String senderName
    ) {

        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.read = read;
        this.readAt = readAt;
        this.active = active;
        this.createdAt = createdAt;
        this.organizationId = organizationId;
        this.recipientId = recipientId;
        this.recipientName = recipientName;
        this.senderId = senderId;
        this.senderName = senderName;
    }
}