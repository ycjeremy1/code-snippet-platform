package com.codeplatform.repository;

import com.codeplatform.entity.SnippetFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SnippetFavoriteRepository extends JpaRepository<SnippetFavorite, Long> {

    boolean existsBySnippetIdAndUserId(Long snippetId, Long userId);

    void deleteBySnippetIdAndUserId(Long snippetId, Long userId);

    Page<SnippetFavorite> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
