package com.lumiora.notification.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lumiora.notification.entity.Notification;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {


    @EntityGraph(attributePaths = {
            "organization",
            "recipient",
            "sender"
    })
    List<Notification>
            findAllByRecipient_IdAndActiveTrueOrderByCreatedAtDesc(
                    Long recipientId
            );


    @EntityGraph(attributePaths = {
            "organization",
            "recipient",
            "sender"
    })
    List<Notification>
            findAllByRecipient_IdAndReadFalseAndActiveTrueOrderByCreatedAtDesc(
                    Long recipientId
            );


    long countByRecipient_IdAndReadFalseAndActiveTrue(
            Long recipientId
    );


    @EntityGraph(attributePaths = {
            "organization",
            "recipient",
            "sender"
    })
    Optional<Notification>
            findByIdAndRecipient_IdAndActiveTrue(
                    Long id,
                    Long recipientId
            );


    @EntityGraph(attributePaths = {
            "organization",
            "recipient",
            "sender"
    })
    Optional<Notification>
            findByIdAndOrganization_Id(
                    Long id,
                    Long organizationId
            );


    @EntityGraph(attributePaths = {
            "organization",
            "recipient",
            "sender"
    })
    List<Notification>
            findAllByOrganization_IdOrderByCreatedAtDesc(
                    Long organizationId
            );
}