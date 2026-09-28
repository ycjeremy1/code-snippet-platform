package com.codeplatform.repository;

import com.codeplatform.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findBySnippetIdOrderByCreatedAtDesc(Long snippetId, Pageable pageable);

    long countBySnippetId(Long snippetId);
}
