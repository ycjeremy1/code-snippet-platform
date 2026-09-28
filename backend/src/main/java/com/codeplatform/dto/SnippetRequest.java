package com.codeplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class SnippetRequest {

    @NotBlank
    @Size(max = 200)
    private String title;

    @Size(max = 1000)
    private String description;

    @NotBlank
    @Size(max = 50)
    private String language;

    @NotBlank
    private String code;

    // Wrapper type (not primitive) so Lombok generates getIsPublic()/setIsPublic(),
    // which Jackson maps to the JSON key "isPublic" - a primitive boolean field here
    // would make Lombok emit isPublic()/setPublic(boolean), which Jackson binds to
    // "public" instead and silently drops the "isPublic" field from the frontend.
    private Boolean isPublic = true;

    @NotEmpty
    private Set<@NotBlank String> tags;
}
