package com.Andaz.assignment.service;

import org.springframework.stereotype.Service;

import com.Andaz.assignment.dto.CreatePostRequest;
import com.Andaz.assignment.dto.PostResponse;
import com.Andaz.assignment.entity.AuthorType;
import com.Andaz.assignment.entity.Post;
import com.Andaz.assignment.repository.BotRepository;
import com.Andaz.assignment.repository.PostRepository;
import com.Andaz.assignment.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final BotRepository botrepository;


    /* Method to create the post */
    public PostResponse createPost(CreatePostRequest request){

        // First check the user or bot exists or not , we call one method called validateAuthor
        validateAuthor(request.getAuthorType(), request.getAuthorId()); /* if not found throw error with message */

        // Now create the post
        Post post = new Post();
        post.setAuthorType(request.getAuthorType());
        post.setAuthorId(request.getAuthorId());
        post.setContent(request.getContent());

        Post savedPost = postRepository.save(post);

        PostResponse response = new PostResponse();
        response.setId(savedPost.getId());
        response.setAuthorType(savedPost.getAuthorType());
        response.setAuthorId(savedPost.getAuthorId());
        response.setContent(savedPost.getContent());
        response.setCreatedAt(savedPost.getCreatedAt());

        return response;

        
    }

    private void validateAuthor(AuthorType type, Long authorId) {

      boolean exists =  switch (type) {
            case USER->
                userRepository.existsById(authorId); /*return the boolean type yes/no */
            case BOT->
                botrepository.existsById(authorId); 

        };

        if(!exists){
            throw new RuntimeException(type+" not found with the id: "+ authorId);
        }
       
        
    }


    
}
