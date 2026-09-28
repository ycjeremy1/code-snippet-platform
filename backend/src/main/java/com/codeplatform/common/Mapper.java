package com.codeplatform.common;

import com.codeplatform.dto.CommentResponse;
import com.codeplatform.dto.NotificationResponse;
import com.codeplatform.dto.SnippetResponse;
import com.codeplatform.dto.UserResponse;
import com.codeplatform.entity.Comment;
import com.codeplatform.entity.Notification;
import com.codeplatform.entity.Snippet;
import com.codeplatform.entity.Tag;
import com.codeplatform.entity.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class Mapper {

    public UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /** Base snippet response, safe to cache (no per-viewer personalization). */
    public SnippetResponse toSnippetResponse(Snippet snippet, long commentCount) {
        return SnippetResponse.builder()
                .id(snippet.getId())
                .title(snippet.getTitle())
                .description(snippet.getDescription())
                .language(snippet.getLanguage())
                .code(snippet.getCode())
                .author(toUserResponse(snippet.getAuthor()))
                .tags(snippet.getTags().stream().map(Tag::getName).collect(Collectors.toSet()))
                .viewCount(snippet.getViewCount())
                .likeCount(snippet.getLikeCount())
                .commentCount(commentCount)
                .isPublic(snippet.getIsPublic())
                .likedByCurrentUser(false)
                .favoritedByCurrentUser(false)
                .createdAt(snippet.getCreatedAt())
                .updatedAt(snippet.getUpdatedAt())
                .build();
    }

    public CommentResponse toCommentResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .user(toUserResponse(comment.getUser()))
                .createdAt(comment.getCreatedAt())
                .build();
    }

    public NotificationResponse toNotificationResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .message(notification.getMessage())
                .relatedSnippetId(notification.getRelatedSnippetId())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
