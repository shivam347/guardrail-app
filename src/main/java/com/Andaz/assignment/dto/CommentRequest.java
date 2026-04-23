package com.Andaz.assignment.dto;

import com.Andaz.assignment.entity.AuthorType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentRequest {

    @NotNull(message = "authorType is required(user or bot)")
    private AuthorType authorType;

    @NotNull
    private Long authorId;


    @NotBlank(message = "content must not be blank")
    private String content;

    @Min(value = 1, message = "depthLevel must be at least 1")
    private Integer depthLevel = 1;


    /* id can be null for top level comments */
    private Long parentCommentId;
    
}
