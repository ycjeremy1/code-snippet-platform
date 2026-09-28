-- Users
CREATE TABLE users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    email           VARCHAR(120) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    bio             VARCHAR(500),
    avatar_url      VARCHAR(500),
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tags
CREATE TABLE tags (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    name    VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Snippets
CREATE TABLE snippets (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(200) NOT NULL,
    description     VARCHAR(1000),
    language        VARCHAR(50)  NOT NULL,
    code            MEDIUMTEXT   NOT NULL,
    author_id       BIGINT       NOT NULL,
    view_count      BIGINT       NOT NULL DEFAULT 0,
    like_count      BIGINT       NOT NULL DEFAULT 0,
    is_public       TINYINT(1)   NOT NULL DEFAULT 1,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_snippet_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_snippets_language ON snippets(language);
CREATE INDEX idx_snippets_created_at ON snippets(created_at);
CREATE FULLTEXT INDEX idx_snippets_title_desc ON snippets(title, description);

-- Snippet <-> Tag (many-to-many)
CREATE TABLE snippet_tags (
    snippet_id  BIGINT NOT NULL,
    tag_id      BIGINT NOT NULL,
    PRIMARY KEY (snippet_id, tag_id),
    CONSTRAINT fk_st_snippet FOREIGN KEY (snippet_id) REFERENCES snippets(id) ON DELETE CASCADE,
    CONSTRAINT fk_st_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Comments
CREATE TABLE comments (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    snippet_id  BIGINT NOT NULL,
    user_id     BIGINT NOT NULL,
    content     VARCHAR(1000) NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comment_snippet FOREIGN KEY (snippet_id) REFERENCES snippets(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_comments_snippet ON comments(snippet_id);

-- Likes
CREATE TABLE snippet_likes (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    snippet_id  BIGINT NOT NULL,
    user_id     BIGINT NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_like_snippet_user (snippet_id, user_id),
    CONSTRAINT fk_like_snippet FOREIGN KEY (snippet_id) REFERENCES snippets(id) ON DELETE CASCADE,
    CONSTRAINT fk_like_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Favorites
CREATE TABLE snippet_favorites (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    snippet_id  BIGINT NOT NULL,
    user_id     BIGINT NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_favorite_snippet_user (snippet_id, user_id),
    CONSTRAINT fk_favorite_snippet FOREIGN KEY (snippet_id) REFERENCES snippets(id) ON DELETE CASCADE,
    CONSTRAINT fk_favorite_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Notifications (populated asynchronously via RabbitMQ consumer)
CREATE TABLE notifications (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_id        BIGINT NOT NULL,
    actor_id            BIGINT,
    type                VARCHAR(30) NOT NULL,
    message             VARCHAR(500) NOT NULL,
    related_snippet_id  BIGINT,
    is_read             TINYINT(1) NOT NULL DEFAULT 0,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_recipient FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_notifications_recipient ON notifications(recipient_id, is_read);
