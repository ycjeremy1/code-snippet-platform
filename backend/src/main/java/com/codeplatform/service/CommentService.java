package com.codeplatform.service;

import com.codeplatform.common.Mapper;
import com.codeplatform.dto.CommentRequest;
import com.codeplatform.dto.CommentResponse;
import com.codeplatform.dto.PageResponse;
import com.codeplatform.entity.Comment;
import com.codeplatform.entity.Notification.NotificationType;
import com.codeplatform.entity.Snippet;
import com.codeplatform.entity.User;
import com.codeplatform.exception.ResourceNotFoundException;
import com.codeplatform.exception.UnauthorizedException;
import com.codeplatform.mq.NotificationMessage;
import com.codeplatform.mq.NotificationProducer;
import com.codeplatform.repository.CommentRepository;
import com.codeplatform.repository.SnippetRepository;
import com.codeplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final SnippetRepository snippetRepository;
    private final UserRepository userRepository;
    private final Mapper mapper;
    private final NotificationProducer notificationProducer;

    @Transactional
    @CacheEvict(cacheNames = "snippetDetail", key = "#snippetId")
    public CommentResponse addComment(Long snippetId, Long userId, CommentRequest request) {
        Snippet snippet = snippetRepository.findById(snippetId)
                .orElseThrow(() -> new ResourceNotFoundException("Snippet not found: " + snippetId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Comment comment = Comment.builder()
                .snippet(snippet)
                .user(user)
                .content(request.getContent())
                .build();
        comment = commentRepository.save(comment);

        notificationProducer.publish(NotificationMessage.builder()
                .recipientId(snippet.getAuthor().getId())
                .actorId(userId)
                .actorUsername(user.getUsername())
                .type(NotificationType.COMMENT)
                .relatedSnippetId(snippetId)
                .relatedSnippetTitle(snippet.getTitle())
                .build());

        return mapper.toCommentResponse(comment);
    }

    public PageResponse<CommentResponse> getComments(Long snippetId, Pageable pageable) {
        Page<Comment> page = commentRepository.findBySnippetIdOrderByCreatedAtDesc(snippetId, pageable);
        return PageResponse.of(page, mapper::toCommentResponse);
    }

    @Transactional
    @CacheEvict(cacheNames = "snippetDetail", key = "#result")
    public Long deleteComment(Long commentId, Long requesterId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));

        boolean isAuthor = comment.getUser().getId().equals(requesterId);
        boolean isSnippetOwner = comment.getSnippet().getAuthor().getId().equals(requesterId);
        if (!isAuthor && !isSnippetOwner) {
            throw new UnauthorizedException("You cannot delete this comment");
        }

        Long snippetId = comment.getSnippet().getId();
        commentRepository.delete(comment);
        return snippetId;
    }
}
