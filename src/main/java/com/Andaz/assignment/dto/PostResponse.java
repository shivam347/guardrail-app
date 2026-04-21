package com.Andaz.assignment.dto;

import java.time.LocalDateTime;

import com.Andaz.assignment.entity.AuthorType;


import lombok.Data;

@Data
public class PostResponse {

   
    private Long id;

    private AuthorType authorType;

    private Long authorId;

    private String content;

    private LocalDateTime createdAt;


    
}
