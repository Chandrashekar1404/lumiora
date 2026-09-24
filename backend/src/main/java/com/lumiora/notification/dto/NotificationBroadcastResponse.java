package com.lumiora.notification.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NotificationBroadcastResponse {

    private Long organizationId;

    private String targetRole;

    private long sentCount;

    private String message;


    public NotificationBroadcastResponse(
            Long organizationId,
            String targetRole,
            long sentCount,
            String message
    ) {

        this.organizationId = organizationId;
        this.targetRole = targetRole;
        this.sentCount = sentCount;
        this.message = message;
    }
}