package com.Andaz.assignment.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class LikeResponse {

    private Long postId;

    private Long userId;

    private Long totalLikes;

    private String message;

    private LocalDateTime createdAt;
    
}
