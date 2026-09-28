package com.codeplatform.service;

import com.codeplatform.entity.Snippet;
import com.codeplatform.entity.Tag;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class SnippetSpecifications {

    private SnippetSpecifications() {
    }

    public static Specification<Snippet> isPublic() {
        return (root, query, cb) -> cb.isTrue(root.get("isPublic"));
    }

    public static Specification<Snippet> keywordMatches(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        String like = "%" + keyword.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), like),
                cb.like(cb.lower(root.get("description")), like)
        );
    }

    public static Specification<Snippet> hasLanguage(String language) {
        if (!StringUtils.hasText(language)) {
            return null;
        }
        return (root, query, cb) -> cb.equal(cb.lower(root.get("language")), language.trim().toLowerCase());
    }

    public static Specification<Snippet> hasTag(String tag) {
        if (!StringUtils.hasText(tag)) {
            return null;
        }
        return (root, query, cb) -> {
            if (query != null) {
                query.distinct(true);
            }
            Join<Snippet, Tag> tags = root.join("tags");
            return cb.equal(cb.lower(tags.get("name")), tag.trim().toLowerCase());
        };
    }

    public static Specification<Snippet> byAuthor(Long authorId) {
        if (authorId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("author").get("id"), authorId);
    }
}
