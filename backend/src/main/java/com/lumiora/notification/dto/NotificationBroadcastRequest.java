package com.lumiora.notification.dto;

import com.lumiora.notification.entity.NotificationType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NotificationBroadcastRequest {

    @NotNull(message = "Organization id is required")
    private Long organizationId;

    /*
     * Optional.
     *
     * Example:
     * STUDENT
     * TRAINER
     * COUNSELOR
     * ACCOUNTANT
     * ADMIN
     *
     * If null/blank -> send to all users in organization.
     */
    private String targetRole;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @NotBlank(message = "Message is required")
    @Size(max = 2000, message = "Message cannot exceed 2000 characters")
    private String message;

    @NotNull(message = "Notification type is required")
    private NotificationType type;
}