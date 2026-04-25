package com.Andaz.assignment.service;

import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Andaz.assignment.dto.CommentRequest;
import com.Andaz.assignment.dto.CommentResponse;
import com.Andaz.assignment.entity.AuthorType;
import com.Andaz.assignment.entity.Comment;
import com.Andaz.assignment.entity.Post;
import com.Andaz.assignment.exception.ResourceNotFoundException;
import com.Andaz.assignment.repository.BotRepository;
import com.Andaz.assignment.repository.CommentRepository;
import com.Andaz.assignment.repository.PostRepository;
import com.Andaz.assignment.repository.UserRepository;
import com.Andaz.assignment.service.RedisGuardRailService.botCommentReservation;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CommentService {

    private final UserRepository userRepo;
    private final PostRepository postRepo;
    private final CommentRepository commentRepo;
    private final BotRepository botRepo;
    private final RedisGuardRailService redisGuardRailService;
    private final NotificationService notificationService;

    /* Method to add the comment to the post */
    @Transactional
    public CommentResponse addComment(Long postId, CommentRequest request) throws BadRequestException {
        // First validate the request author Type and author id means first we know
        // comment is added by bot or a user
        validateAuthor(request.getAuthorType(), request.getAuthorId());

        // first we find the post with postid
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("post not found with postid" + postId));

        Comment parentComment = null;
        int depthLevel = 1;

        // now we fetch the parent comment id given in request from comment repo
        if (request.getParentCommentId() != null) {
            parentComment = commentRepo.findById(request.getParentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("No parent comment id is found"));
            // Now check parent comment id belongs to this post or not
            if (!parentComment.getPost().getId().equals(postId)) {
                throw new BadRequestException("Parent comment does not belong to the given post");
            }
            // Now we will increase the depth level
            depthLevel = parentComment.getDepthLevel() + 1;
        }

        // Now we should validate it from the redis
        redisGuardRailService.validateDepthLevel(depthLevel, request.getAuthorType());

        // Now we will implement the cool down period , bot can interact once in 10 min
        // for a post or a comment
        // first we need to find human id
        botCommentReservation reservation = null;
        String botName = null;
        if (request.getAuthorType() == AuthorType.BOT) {
            Long humanId = getHumanId(post, parentComment);
            // Now we need to set the coolDown period using redisTemplate service
           reservation = redisGuardRailService.reservedBotComment(postId, request.getAuthorId(), humanId);
           botName = botRepo.findById(request.getAuthorId()).orElseThrow(() -> new ResourceNotFoundException("BOT not found with id: " + request.getAuthorId())).getName();
        }

        // Now we should create the comment
        try {

            Comment comment = new Comment();
            comment.setPost(post);
            comment.setParentComment(parentComment);
            comment.setContent(request.getContent());
            comment.setAuthorType(request.getAuthorType());
            comment.setAuthorId(request.getAuthorId());
            comment.setDepthLevel(depthLevel);

            // save it to the commentRepo
            Comment savedComment = commentRepo.save(comment);

            // now we have do viralitycount based on interactionType
            // but before that we need to get what kind of interaction we can have on adding
            // comment, bot add comment,
            // or human add comment
            InteractionType interactionType = (request.getAuthorType() == AuthorType.BOT) ? InteractionType.BOT_REPLY
                    : InteractionType.HUMAN_COMMENT;

            // Now call rediservice and increment the viralCount
            redisGuardRailService.viralityScore(postId, interactionType);
            triggerNotificationIfNeeded(post, request.getAuthorType(), botName);
            CommentResponse response = new CommentResponse();
            response.setId(savedComment.getId());
            response.setPostId(savedComment.getPost().getId());
            response.setParentCommentId(savedComment.getParentComment() != null ? savedComment.getParentComment().getId() : null);
            response.setAuthorType(savedComment.getAuthorType());
            response.setAuthorId(savedComment.getAuthorId());
            response.setContent(savedComment.getContent());
            response.setDepthLevel(savedComment.getDepthLevel());
            response.setCreatedAt(savedComment.getCreatedAt());
            return response;

        } catch (RuntimeException ex) {
             // First roll back
             redisGuardRailService.rollBack(reservation);
             throw ex;
        }

    }

    /* helper method for triggering notification */
    private void triggerNotificationIfNeeded(Post post, AuthorType authorType, String botName) {
        // trigger notification only when author type is bot and post author type is user
        if(post.getAuthorType() != AuthorType.USER || authorType != AuthorType.BOT){
            return;
        }
       
        String notificationMessg = "BOT "+botName+" replied to your post";
        notificationService.handleBotInteraction(post.getAuthorId(), notificationMessg);

    }

    private Long getHumanId(Post post, Comment parentComment) {
        // first checks the parentComment type should be the user
        if (parentComment != null && parentComment.getAuthorType() == AuthorType.USER) {
            return parentComment.getAuthorId();
        }
        // if bot is interacting by simply making comment not replying to any comment
        // interacting with the human post, so check
        // post belongs to user
        if (post.getAuthorType() == AuthorType.USER) {
            return post.getAuthorId();
        }
        return null;
    }

    /* Method to validate the author is user or a bot */
    private void validateAuthor(AuthorType type, Long authorId) {

        boolean exists = switch (type) {
            case USER ->
                userRepo.existsById(authorId); /* return the boolean type yes/no */
            case BOT ->
                botRepo.existsById(authorId);

        };

        if (!exists) {
            throw new RuntimeException(type + " not found with the id: " + authorId);

        }

    }
}
