package com.lumiora.notification.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lumiora.entity.auth.User;
import com.lumiora.entity.auth.UserStatus;

import com.lumiora.notification.dto.NotificationBroadcastRequest;
import com.lumiora.notification.dto.NotificationBroadcastResponse;
import com.lumiora.notification.dto.NotificationCreateRequest;
import com.lumiora.notification.dto.NotificationResponse;

import com.lumiora.notification.entity.Notification;
import com.lumiora.notification.repository.NotificationRepository;
import com.lumiora.notification.service.NotificationService;

import com.lumiora.organization.entity.Organization;
import com.lumiora.organization.repository.OrganizationRepository;

import com.lumiora.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl
        implements NotificationService {


    private final NotificationRepository notificationRepository;

    private final UserRepository userRepository;

    private final OrganizationRepository organizationRepository;


    /*
     * Roles which can be targeted through broadcast.
     */
    private static final Set<String> BROADCAST_ROLES =
            Set.of(
                    "ADMIN",
                    "TRAINER",
                    "STUDENT",
                    "COUNSELOR",
                    "ACCOUNTANT"
            );


    // =========================================================
    // CREATE NOTIFICATION
    // =========================================================

    @Override
    public NotificationResponse createNotification(
            NotificationCreateRequest request,
            Authentication authentication
    ) {

        User currentUser =
                getCurrentUser(authentication);

        validateAdminAccess(currentUser);


        User recipient =
                userRepository
                        .findById(request.getRecipientUserId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Recipient user not found with id: "
                                                + request.getRecipientUserId()
                                )
                        );


        if (recipient.getStatus()
                != UserStatus.ACTIVE) {

            throw new IllegalArgumentException(
                    "Recipient user is not active"
            );
        }


        if (recipient.getOrganization() == null) {

            throw new IllegalArgumentException(
                    "Recipient is not assigned to an organization"
            );
        }


        Long recipientOrganizationId =
                recipient.getOrganization().getId();


        /*
         * ADMIN can only send inside own organization.
         */
        if ("ADMIN".equals(
                currentUser.getRole().getName())) {

            Long currentOrganizationId =
                    getOrganizationId(currentUser);

            if (!currentOrganizationId.equals(
                    recipientOrganizationId)) {

                throw new IllegalArgumentException(
                        "You cannot send notifications to another organization"
                );
            }


            /*
             * ADMIN cannot target SUPER_ADMIN.
             */
            if (recipient.getRole() != null
                    && "SUPER_ADMIN".equals(
                    recipient.getRole().getName())) {

                throw new IllegalArgumentException(
                        "ADMIN cannot send notifications to SUPER_ADMIN"
                );
            }
        }


        Notification notification =
                new Notification();

        notification.setTitle(
                request.getTitle()
        );

        notification.setMessage(
                request.getMessage()
        );

        notification.setType(
                request.getType()
        );

        notification.setRead(false);

        notification.setReadAt(null);

        notification.setActive(true);

        notification.setCreatedAt(
                LocalDateTime.now()
        );

        notification.setOrganization(
                recipient.getOrganization()
        );

        notification.setRecipient(
                recipient
        );

        notification.setSender(
                currentUser
        );


        Notification saved =
                notificationRepository.save(
                        notification
                );


        return toResponse(saved);
    }


    // =========================================================
    // BROADCAST
    // =========================================================

    @Override
    public NotificationBroadcastResponse broadcastNotification(
            NotificationBroadcastRequest request,
            Authentication authentication
    ) {

        User currentUser =
                getCurrentUser(authentication);

        validateAdminAccess(currentUser);


        Organization organization =
                organizationRepository
                        .findById(
                                request.getOrganizationId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Organization not found with id: "
                                                + request.getOrganizationId()
                                )
                        );


        /*
         * ADMIN can only broadcast in own organization.
         */
        if ("ADMIN".equals(
                currentUser.getRole().getName())) {

            Long currentOrganizationId =
                    getOrganizationId(currentUser);

            if (!currentOrganizationId.equals(
                    organization.getId())) {

                throw new IllegalArgumentException(
                        "ADMIN can broadcast only inside their own organization"
                );
            }
        }


        String targetRole =
                request.getTargetRole();


        if (targetRole != null
                && !targetRole.isBlank()) {

            targetRole =
                    targetRole.trim()
                            .toUpperCase();


            if (!BROADCAST_ROLES.contains(
                    targetRole)) {

                throw new IllegalArgumentException(
                        "Invalid target role: "
                                + targetRole
                );
            }
        }


        List<User> recipients;


        /*
         * No role -> all users in organization.
         */
        if (targetRole == null
                || targetRole.isBlank()) {

            recipients =
                    userRepository
                            .findAllByOrganization_Id(
                                    organization.getId()
                            );

        } else {

            recipients =
                    userRepository
                            .findAllByOrganization_IdAndRole_Name(
                                    organization.getId(),
                                    targetRole
                            );
        }


        /*
         * Only active users receive notifications.
         */
        recipients =
                recipients.stream()
                        .filter(user ->
                                user.getStatus()
                                        == UserStatus.ACTIVE
                        )
                        .toList();


        if (recipients.isEmpty()) {

            throw new IllegalArgumentException(
                    "No active users found for the selected broadcast"
            );
        }


        List<Notification> notifications =
                recipients.stream()
                        .map(recipient -> {

                            Notification notification =
                                    new Notification();

                            notification.setTitle(
                                    request.getTitle()
                            );

                            notification.setMessage(
                                    request.getMessage()
                            );

                            notification.setType(
                                    request.getType()
                            );

                            notification.setRead(
                                    false
                            );

                            notification.setReadAt(
                                    null
                            );

                            notification.setActive(
                                    true
                            );

                            notification.setCreatedAt(
                                    LocalDateTime.now()
                            );

                            notification.setOrganization(
                                    organization
                            );

                            notification.setRecipient(
                                    recipient
                            );

                            notification.setSender(
                                    currentUser
                            );

                            return notification;

                        })
                        .toList();


        notificationRepository.saveAll(
                notifications
        );


        String responseTarget =
                targetRole == null
                        || targetRole.isBlank()
                        ? "ALL"
                        : targetRole;


        return new NotificationBroadcastResponse(
                organization.getId(),
                responseTarget,
                notifications.size(),
                "Notification broadcast successfully"
        );
    }


    // =========================================================
    // GET MY NOTIFICATIONS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(
            Authentication authentication
    ) {

        User currentUser =
                getCurrentUser(authentication);

        return notificationRepository
                .findAllByRecipient_IdAndActiveTrueOrderByCreatedAtDesc(
                        currentUser.getId()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }


    // =========================================================
    // GET MY NOTIFICATION
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getMyNotification(
            Long notificationId,
            Authentication authentication
    ) {

        User currentUser =
                getCurrentUser(authentication);


        Notification notification =
                notificationRepository
                        .findByIdAndRecipient_IdAndActiveTrue(
                                notificationId,
                                currentUser.getId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Notification not found"
                                )
                        );


        return toResponse(notification);
    }


    // =========================================================
    // MARK AS READ
    // =========================================================

    @Override
    public NotificationResponse markAsRead(
            Long notificationId,
            Authentication authentication
    ) {

        User currentUser =
                getCurrentUser(authentication);


        Notification notification =
                notificationRepository
                        .findByIdAndRecipient_IdAndActiveTrue(
                                notificationId,
                                currentUser.getId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Notification not found"
                                )
                        );


        if (!notification.isRead()) {

            notification.setRead(true);

            notification.setReadAt(
                    LocalDateTime.now()
            );

            notificationRepository.save(
                    notification
            );
        }


        return toResponse(notification);
    }


    // =========================================================
    // MARK ALL AS READ
    // =========================================================

    @Override
    public long markAllAsRead(
            Authentication authentication
    ) {

        User currentUser =
                getCurrentUser(authentication);


        List<Notification> notifications =
                notificationRepository
                        .findAllByRecipient_IdAndReadFalseAndActiveTrueOrderByCreatedAtDesc(
                                currentUser.getId()
                        );


        LocalDateTime now =
                LocalDateTime.now();


        notifications.forEach(
                notification -> {

                    notification.setRead(true);

                    notification.setReadAt(now);
                }
        );


        if (!notifications.isEmpty()) {

            notificationRepository.saveAll(
                    notifications
            );
        }


        return notifications.size();
    }


    // =========================================================
    // DEACTIVATE
    // =========================================================

    @Override
    public void deactivateNotification(
            Long notificationId,
            Authentication authentication
    ) {

        User currentUser =
                getCurrentUser(authentication);

        validateAdminAccess(currentUser);


        Notification notification;


        if ("SUPER_ADMIN".equals(
                currentUser.getRole().getName())) {

            notification =
                    notificationRepository
                            .findById(
                                    notificationId
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Notification not found"
                                    )
                            );

        } else {

            Long organizationId =
                    getOrganizationId(currentUser);

            notification =
                    notificationRepository
                            .findByIdAndOrganization_Id(
                                    notificationId,
                                    organizationId
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Notification not found in your organization"
                                    )
                            );
        }


        notification.setActive(false);

        notificationRepository.save(
                notification
        );
    }


    // =========================================================
    // CURRENT USER
    // =========================================================

    private User getCurrentUser(
            Authentication authentication
    ) {

        return userRepository
                .findByEmail(
                        authentication.getName()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Authenticated user not found"
                        )
                );
    }


    // =========================================================
    // ADMIN VALIDATION
    // =========================================================

    private void validateAdminAccess(
            User user
    ) {

        String role =
                user.getRole()
                        .getName();


        if (!"SUPER_ADMIN".equals(role)
                && !"ADMIN".equals(role)) {

            throw new IllegalArgumentException(
                    "Only SUPER_ADMIN and ADMIN can manage notifications"
            );
        }
    }


    // =========================================================
    // ORGANIZATION ID
    // =========================================================

    private Long getOrganizationId(
            User user
    ) {

        if (user.getOrganization() == null) {

            throw new IllegalArgumentException(
                    "User is not assigned to an organization"
            );
        }

        return user.getOrganization().getId();
    }


    // =========================================================
    // RESPONSE MAPPING
    // =========================================================

    private NotificationResponse toResponse(
            Notification notification
    ) {

        String recipientName =
                buildName(
                        notification.getRecipient()
                );


        String senderName =
                notification.getSender() != null
                        ? buildName(
                        notification.getSender()
                )
                        : null;


        return new NotificationResponse(

                notification.getId(),

                notification.getTitle(),

                notification.getMessage(),

                notification.getType(),

                notification.isRead(),

                notification.getReadAt(),

                notification.isActive(),

                notification.getCreatedAt(),

                notification.getOrganization() != null
                        ? notification.getOrganization().getId()
                        : null,

                notification.getRecipient() != null
                        ? notification.getRecipient().getId()
                        : null,

                recipientName,

                notification.getSender() != null
                        ? notification.getSender().getId()
                        : null,

                senderName
        );
    }


    private String buildName(
            User user
    ) {

        if (user == null) {
            return null;
        }


        String firstName =
                user.getFirstName() != null
                        ? user.getFirstName()
                        : "";


        String lastName =
                user.getLastName() != null
                        ? user.getLastName()
                        : "";


        return (
                firstName
                        + " "
                        + lastName
        ).trim();
    }
}