package com.codeplatform.service;

import com.codeplatform.common.Mapper;
import com.codeplatform.dto.PageResponse;
import com.codeplatform.dto.SnippetRequest;
import com.codeplatform.dto.SnippetResponse;
import com.codeplatform.entity.Snippet;
import com.codeplatform.entity.User;
import com.codeplatform.exception.ResourceNotFoundException;
import com.codeplatform.exception.UnauthorizedException;
import com.codeplatform.mq.NotificationMessage;
import com.codeplatform.mq.NotificationProducer;
import com.codeplatform.repository.CommentRepository;
import com.codeplatform.repository.SnippetFavoriteRepository;
import com.codeplatform.repository.SnippetLikeRepository;
import com.codeplatform.repository.SnippetRepository;
import com.codeplatform.repository.UserRepository;
import com.codeplatform.entity.SnippetFavorite;
import com.codeplatform.entity.SnippetLike;
import com.codeplatform.entity.Notification.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cache-aside strategy: reads first consult Redis (via {@code @Cacheable}); on a miss the DB is
 * queried and the result populated back into Redis with a short TTL (see {@link com.codeplatform.config.RedisConfig}).
 * Writes go straight to MySQL and evict the affected cache entries so the next read repopulates
 * the cache from the source of truth, rather than the application ever writing into Redis directly.
 */
@Service
@RequiredArgsConstructor
public class SnippetService {

    private final SnippetRepository snippetRepository;
    private final CommentRepository commentRepository;
    private final SnippetLikeRepository likeRepository;
    private final SnippetFavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final TagService tagService;
    private final Mapper mapper;
    private final NotificationProducer notificationProducer;

    @Transactional
    public SnippetResponse create(Long authorId, SnippetRequest request) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Snippet snippet = Snippet.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .language(request.getLanguage())
                .code(request.getCode())
                .author(author)
                .isPublic(request.getIsPublic() == null || request.getIsPublic())
                .tags(tagService.resolveOrCreate(request.getTags()))
                .build();

        snippet = snippetRepository.save(snippet);
        return mapper.toSnippetResponse(snippet, 0);
    }

    @Transactional
    @CacheEvict(cacheNames = "snippetDetail", key = "#snippetId")
    public SnippetResponse update(Long snippetId, Long requesterId, SnippetRequest request) {
        Snippet snippet = findEntity(snippetId);
        if (!snippet.getAuthor().getId().equals(requesterId)) {
            throw new UnauthorizedException("You can only edit your own snippets");
        }

        snippet.setTitle(request.getTitle());
        snippet.setDescription(request.getDescription());
        snippet.setLanguage(request.getLanguage());
        snippet.setCode(request.getCode());
        snippet.setIsPublic(request.getIsPublic() == null || request.getIsPublic());
        snippet.setTags(tagService.resolveOrCreate(request.getTags()));

        snippet = snippetRepository.save(snippet);
        long commentCount = commentRepository.countBySnippetId(snippetId);
        return mapper.toSnippetResponse(snippet, commentCount);
    }

    @Transactional
    @CacheEvict(cacheNames = "snippetDetail", key = "#snippetId")
    public void delete(Long snippetId, Long requesterId) {
        Snippet snippet = findEntity(snippetId);
        if (!snippet.getAuthor().getId().equals(requesterId)) {
            throw new UnauthorizedException("You can only delete your own snippets");
        }
        snippetRepository.delete(snippet);
    }

    /**
     * Cached detail lookup. The cached value intentionally excludes viewer-specific fields
     * (liked/favorited by current user) so the same cache entry is safely shared across viewers;
     * those flags are applied on top after the cached/db value comes back (see {@link #getById}).
     */
    @Cacheable(cacheNames = "snippetDetail", key = "#snippetId")
    public SnippetResponse getCachedDetail(Long snippetId) {
        Snippet snippet = findEntity(snippetId);
        long commentCount = commentRepository.countBySnippetId(snippetId);
        return mapper.toSnippetResponse(snippet, commentCount);
    }

    public SnippetResponse getById(Long snippetId, Long viewerId) {
        SnippetResponse response = getCachedDetail(snippetId);

        // Fire-and-forget view counter; not reflected in the cached entry until it naturally expires.
        snippetRepository.incrementViewCount(snippetId);

        if (viewerId != null) {
            response.setLikedByCurrentUser(likeRepository.existsBySnippetIdAndUserId(snippetId, viewerId));
            response.setFavoritedByCurrentUser(favoriteRepository.existsBySnippetIdAndUserId(snippetId, viewerId));
        }
        return response;
    }

    @Cacheable(cacheNames = "snippetSearch", key = "T(String).format('%s|%s|%s|%s|%d|%d', #keyword, #language, #tag, #authorId, #pageable.pageNumber, #pageable.pageSize)")
    public PageResponse<SnippetResponse> search(String keyword, String language, String tag, Long authorId, Pageable pageable) {
        Specification<Snippet> spec = Specification.where(SnippetSpecifications.isPublic())
                .and(SnippetSpecifications.keywordMatches(keyword))
                .and(SnippetSpecifications.hasLanguage(language))
                .and(SnippetSpecifications.hasTag(tag))
                .and(SnippetSpecifications.byAuthor(authorId));

        Page<Snippet> page = snippetRepository.findAll(spec, pageable);
        return PageResponse.of(page, s -> mapper.toSnippetResponse(s, commentRepository.countBySnippetId(s.getId())));
    }

    public PageResponse<SnippetResponse> getMySnippets(Long authorId, Pageable pageable) {
        Page<Snippet> page = snippetRepository.findByAuthorId(authorId, pageable);
        return PageResponse.of(page, s -> mapper.toSnippetResponse(s, commentRepository.countBySnippetId(s.getId())));
    }

    public PageResponse<SnippetResponse> getMyFavorites(Long userId, Pageable pageable) {
        Page<SnippetFavorite> page = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return PageResponse.of(page, f -> mapper.toSnippetResponse(f.getSnippet(), commentRepository.countBySnippetId(f.getSnippet().getId())));
    }

    @Transactional
    @CacheEvict(cacheNames = "snippetDetail", key = "#snippetId")
    public boolean toggleLike(Long snippetId, Long userId) {
        Snippet snippet = findEntity(snippetId);

        boolean alreadyLiked = likeRepository.existsBySnippetIdAndUserId(snippetId, userId);
        if (alreadyLiked) {
            likeRepository.deleteBySnippetIdAndUserId(snippetId, userId);
            snippetRepository.adjustLikeCount(snippetId, -1);
            return false;
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        likeRepository.save(SnippetLike.builder().snippet(snippet).user(user).build());
        snippetRepository.adjustLikeCount(snippetId, 1);

        notificationProducer.publish(NotificationMessage.builder()
                .recipientId(snippet.getAuthor().getId())
                .actorId(userId)
                .actorUsername(user.getUsername())
                .type(NotificationType.LIKE)
                .relatedSnippetId(snippetId)
                .relatedSnippetTitle(snippet.getTitle())
                .build());

        return true;
    }

    @Transactional
    public boolean toggleFavorite(Long snippetId, Long userId) {
        Snippet snippet = findEntity(snippetId);

        boolean alreadyFavorited = favoriteRepository.existsBySnippetIdAndUserId(snippetId, userId);
        if (alreadyFavorited) {
            favoriteRepository.deleteBySnippetIdAndUserId(snippetId, userId);
            return false;
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        favoriteRepository.save(SnippetFavorite.builder().snippet(snippet).user(user).build());

        notificationProducer.publish(NotificationMessage.builder()
                .recipientId(snippet.getAuthor().getId())
                .actorId(userId)
                .actorUsername(user.getUsername())
                .type(NotificationType.FAVORITE)
                .relatedSnippetId(snippetId)
                .relatedSnippetTitle(snippet.getTitle())
                .build());

        return true;
    }

    private Snippet findEntity(Long id) {
        return snippetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Snippet not found: " + id));
    }
}
