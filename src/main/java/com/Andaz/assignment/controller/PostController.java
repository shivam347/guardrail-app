package com.Andaz.assignment.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Andaz.assignment.dto.CreatePostRequest;
import com.Andaz.assignment.dto.PostResponse;
import com.Andaz.assignment.service.PostService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/posts")
@AllArgsConstructor
public class PostController {

    private final PostService postService;




    @PostMapping
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody CreatePostRequest request){
       
        PostResponse response =  postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
}
