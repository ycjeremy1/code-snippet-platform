package com.codeplatform.repository;

import com.codeplatform.entity.Snippet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SnippetRepository extends JpaRepository<Snippet, Long>, JpaSpecificationExecutor<Snippet> {

    Page<Snippet> findByAuthorId(Long authorId, Pageable pageable);

    @Modifying
    @Query("UPDATE Snippet s SET s.viewCount = s.viewCount + 1 WHERE s.id = :id")
    void incrementViewCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Snippet s SET s.likeCount = s.likeCount + :delta WHERE s.id = :id")
    void adjustLikeCount(@Param("id") Long id, @Param("delta") long delta);
}
