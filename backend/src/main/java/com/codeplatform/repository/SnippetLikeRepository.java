package com.codeplatform.repository;

import com.codeplatform.entity.SnippetLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SnippetLikeRepository extends JpaRepository<SnippetLike, Long> {

    Optional<SnippetLike> findBySnippetIdAndUserId(Long snippetId, Long userId);

    boolean existsBySnippetIdAndUserId(Long snippetId, Long userId);

    void deleteBySnippetIdAndUserId(Long snippetId, Long userId);
}
