package com.Andaz.assignment.controller;


import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Andaz.assignment.dto.CommentRequest;
import com.Andaz.assignment.dto.CommentResponse;
import com.Andaz.assignment.dto.CreatePostRequest;
import com.Andaz.assignment.dto.LikeRequest;
import com.Andaz.assignment.dto.LikeResponse;
import com.Andaz.assignment.dto.PostResponse;
import com.Andaz.assignment.service.CommentService;
import com.Andaz.assignment.service.PostService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/posts")
@AllArgsConstructor
public class PostController {

    private final PostService postService;
    private final CommentService commentService;




    @PostMapping
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody CreatePostRequest request){
       
        PostResponse response =  postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    /* PostMapping to add the comment to the post */
    @PostMapping("/{postid}/comments")
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long postid, @Valid @RequestBody CommentRequest request) throws BadRequestException{
       CommentResponse response =  commentService.addComment(postid, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
        
    }

    /* Post Mapping for like the post */
    @PostMapping("/{postId}/like")
    public ResponseEntity<LikeResponse> likePost(@PathVariable Long postId, @Valid @RequestBody LikeRequest request){
       LikeResponse response =  postService.likePost(postId, request);
       return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }
    
}
