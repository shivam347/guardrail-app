package com.Andaz.assignment.service;

import org.springframework.stereotype.Service;

import com.Andaz.assignment.dto.CreatePostRequest;
import com.Andaz.assignment.dto.LikeRequest;
import com.Andaz.assignment.dto.LikeResponse;
import com.Andaz.assignment.dto.PostResponse;
import com.Andaz.assignment.entity.AuthorType;
import com.Andaz.assignment.entity.Post;
import com.Andaz.assignment.entity.PostLike;
import com.Andaz.assignment.entity.User;
import com.Andaz.assignment.exception.BadRequestException;
import com.Andaz.assignment.exception.ResourceNotFoundException;
import com.Andaz.assignment.repository.BotRepository;
import com.Andaz.assignment.repository.PostLikeRepository;
import com.Andaz.assignment.repository.PostRepository;
import com.Andaz.assignment.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final BotRepository botrepository;
    private final PostLikeRepository likeRepo;

    private final RedisGuardRailService redisGuardService;

    /* Method to create the post */
    @Transactional
    public PostResponse createPost(CreatePostRequest request) {

        // First check the user or bot exists or not , we call one method called
        // validateAuthor
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

        boolean exists = switch (type) {
            case USER ->
                userRepository.existsById(authorId); /* return the boolean type yes/no */
            case BOT ->
                botrepository.existsById(authorId);

        };

        if (!exists) {
            throw new RuntimeException(type + " not found with the id: " + authorId);
        }

    }

    /* Method to like the post */
    @Transactional
    public LikeResponse likePost(Long postId, LikeRequest request) {

        // First find the postid in postrepo
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("No postid in the exists"));
        // Now find the author id exists or not
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("No user with this id exits"));

        // Before moving forward first check user already like the post or not, if
        // userid and postid already exists in like repo then return exception
        if (likeRepo.existsByPostIdAndUserId(postId, request.getUserId())) {
            throw new BadRequestException("User already liked the post");
        }

        // Create one postLike Object
        PostLike like = new PostLike();
        like.setPost(post);
        like.setUser(user);

        PostLike savedLike = likeRepo.save(like);

        Long totalLike = likeRepo.countByPostId(postId);
        redisGuardService.viralityScore(postId, InteractionType.HUMAN_LIKE);

        // Create response and return the response
        LikeResponse response = new LikeResponse();

        response.setPostId(postId);
        response.setUserId(request.getUserId());
        response.setTotalLikes(totalLike);
        response.setMessage("Post liked successfully!!");
        response.setCreatedAt(savedLike.getCreatedAt());

        return response;

    }

}
