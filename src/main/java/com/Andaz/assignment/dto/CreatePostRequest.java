package com.Andaz.assignment.dto;

import com.Andaz.assignment.entity.AuthorType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePostRequest {

    @NotNull(message = "authorId is required")
    private Long authorId;

    @NotNull(message = "Author type cannot be null(user or bot is required)")
    private AuthorType authorType;

    @NotBlank(message = "content cannot be blank")
    private String content;
    
}
