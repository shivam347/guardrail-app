package com.Andaz.assignment.dto;

import java.time.LocalDateTime;

import com.Andaz.assignment.entity.AuthorType;

import lombok.Data;

@Data
public class CommentResponse {

    private Long id;
    private Long postId;
    private String content;
    private Integer depthLevel;
    private Long parentCommentId;
    private Long authorId;
    private AuthorType authorType;
    private LocalDateTime createdAt;
    
}
