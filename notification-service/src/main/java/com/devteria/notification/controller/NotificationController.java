package com.devteria.notification.controller;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.devteria.event.dto.NotificationEvent;
import com.devteria.notification.dto.request.Recipient;
import com.devteria.notification.dto.request.SendEmailRequest;
import com.devteria.notification.service.EmailService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {

    EmailService emailService;

    @KafkaListener(topics = "notification-delivery", groupId = "notification-service")
    public void listenNotificationDelivery(NotificationEvent event) {
        log.info("Received event: {}", event);
        emailService.sendEmail(SendEmailRequest.builder()
                .to(Recipient.builder()
                        .email(event.getRecipient())
                        .name("Testing")
                        .build())
                .subject(event.getSubject())
                .htmlContent(event.getBody())
                .build());
    }
}
