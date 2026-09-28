package com.codeplatform.mq;

import com.codeplatform.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/** Publishes notification events; the actual DB write happens asynchronously in {@link NotificationConsumer}. */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publish(NotificationMessage message) {
        // Don't notify users about their own actions on their own content.
        if (message.getActorId() != null && message.getActorId().equals(message.getRecipientId())) {
            return;
        }
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                    message
            );
        } catch (Exception e) {
            // Notification delivery is best-effort; never fail the user-facing request because of it.
            log.warn("Failed to publish notification event: {}", e.getMessage());
        }
    }
}
