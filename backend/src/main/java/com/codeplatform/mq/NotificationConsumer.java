package com.codeplatform.mq;

import com.codeplatform.config.RabbitMQConfig;
import com.codeplatform.entity.Notification;
import com.codeplatform.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handle(NotificationMessage message) {
        Notification notification = Notification.builder()
                .recipientId(message.getRecipientId())
                .actorId(message.getActorId())
                .type(message.getType())
                .relatedSnippetId(message.getRelatedSnippetId())
                .message(buildMessage(message))
                .isRead(false)
                .build();

        notificationRepository.save(notification);
        log.debug("Persisted {} notification for recipient {}", message.getType(), message.getRecipientId());
    }

    private String buildMessage(NotificationMessage message) {
        String actor = message.getActorUsername() != null ? message.getActorUsername() : "Someone";
        String title = message.getRelatedSnippetTitle() != null ? message.getRelatedSnippetTitle() : "your snippet";

        return switch (message.getType()) {
            case LIKE -> actor + " liked " + title;
            case COMMENT -> actor + " commented on " + title;
            case FAVORITE -> actor + " favorited " + title;
        };
    }
}
