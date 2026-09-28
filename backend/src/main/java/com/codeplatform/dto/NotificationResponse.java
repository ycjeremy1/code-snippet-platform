package com.codeplatform.dto;

import com.codeplatform.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private Notification.NotificationType type;
    private String message;
    private Long relatedSnippetId;
    // Boolean wrapper, not primitive - see SnippetRequest.isPublic for why: a primitive
    // "isX" field makes Lombok emit isRead()/setRead(boolean), which Jackson serializes
    // as "read" instead of "isRead".
    private Boolean isRead;
    private LocalDateTime createdAt;
}
