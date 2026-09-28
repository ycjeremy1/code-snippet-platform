package com.codeplatform.controller;

import com.codeplatform.common.SecurityUtils;
import com.codeplatform.dto.PageResponse;
import com.codeplatform.dto.SnippetRequest;
import com.codeplatform.dto.SnippetResponse;
import com.codeplatform.service.SnippetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/snippets")
@RequiredArgsConstructor
public class SnippetController {

    private final SnippetService snippetService;

    @PostMapping
    public ResponseEntity<SnippetResponse> create(@Valid @RequestBody SnippetRequest request) {
        Long userId = SecurityUtils.requireUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(snippetService.create(userId, request));
    }

    @GetMapping
    public ResponseEntity<PageResponse<SnippetResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) Long authorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50), Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(snippetService.search(keyword, language, tag, authorId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SnippetResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(snippetService.getById(id, SecurityUtils.currentUserId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SnippetResponse> update(@PathVariable Long id, @Valid @RequestBody SnippetRequest request) {
        return ResponseEntity.ok(snippetService.update(id, SecurityUtils.requireUserId(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        snippetService.delete(id, SecurityUtils.requireUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Map<String, Boolean>> toggleLike(@PathVariable Long id) {
        boolean liked = snippetService.toggleLike(id, SecurityUtils.requireUserId());
        return ResponseEntity.ok(Map.of("liked", liked));
    }

    @PostMapping("/{id}/favorite")
    public ResponseEntity<Map<String, Boolean>> toggleFavorite(@PathVariable Long id) {
        boolean favorited = snippetService.toggleFavorite(id, SecurityUtils.requireUserId());
        return ResponseEntity.ok(Map.of("favorited", favorited));
    }

    @GetMapping("/mine")
    public ResponseEntity<PageResponse<SnippetResponse>> mySnippets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50), Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(snippetService.getMySnippets(SecurityUtils.requireUserId(), pageable));
    }

    @GetMapping("/favorites")
    public ResponseEntity<PageResponse<SnippetResponse>> myFavorites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return ResponseEntity.ok(snippetService.getMyFavorites(SecurityUtils.requireUserId(), pageable));
    }
}
