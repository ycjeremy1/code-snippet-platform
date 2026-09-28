package com.codeplatform.controller;

import com.codeplatform.common.SecurityUtils;
import com.codeplatform.dto.CommentRequest;
import com.codeplatform.dto.CommentResponse;
import com.codeplatform.dto.PageResponse;
import com.codeplatform.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/snippets/{snippetId}/comments")
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long snippetId, @Valid @RequestBody CommentRequest request) {
        Long userId = SecurityUtils.requireUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.addComment(snippetId, userId, request));
    }

    @GetMapping("/comments/snippet/{snippetId}")
    public ResponseEntity<PageResponse<CommentResponse>> getComments(
            @PathVariable Long snippetId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return ResponseEntity.ok(commentService.getComments(snippetId, pageable));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId, SecurityUtils.requireUserId());
        return ResponseEntity.noContent().build();
    }
}
