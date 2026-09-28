package com.codeplatform.mq;

import com.codeplatform.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage implements Serializable {
    private Long recipientId;
    private Long actorId;
    private String actorUsername;
    private Notification.NotificationType type;
    private Long relatedSnippetId;
    private String relatedSnippetTitle;
}
