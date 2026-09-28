package com.codeplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SnippetResponse implements Serializable {
    private Long id;
    private String title;
    private String description;
    private String language;
    private String code;
    private UserResponse author;
    private Set<String> tags;
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    // Boolean wrapper, not primitive: see the comment on SnippetRequest.isPublic for why
    // (Lombok's getIsPublic()/setIsPublic() naming is what makes Jackson serialize this as "isPublic").
    private Boolean isPublic;
    private boolean likedByCurrentUser;
    private boolean favoritedByCurrentUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
