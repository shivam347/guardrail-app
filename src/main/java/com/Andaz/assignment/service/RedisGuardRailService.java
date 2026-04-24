package com.Andaz.assignment.service;


import com.Andaz.assignment.entity.AuthorType;
import com.Andaz.assignment.exception.TooManyRequestException;
import java.time.Duration;

import org.apache.coyote.BadRequestException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class RedisGuardRailService {

    // First we need redis dependency over here to use it
    private final StringRedisTemplate stringRedisTemplate;

    /* a SINGLE post does not have 100 bot replies so we will store 100 in variable to check the replies and make them constant so no one can change it */
    private static final Long BOT_REPLY_CAP = 100L;

    /* Same constant for human and bot interaction , same bot cannot interaction same human more than once in 10 minutes */
    private static final Duration BOT_HUMAN_DURATION = Duration.ofMinutes(10);
    
    /* First method to get the virality score , for that i need to pass two things , on which post interaction happen, and what type of interaction happen */
    public void viralityScore(Long postId, InteractionType interactionType){
        stringRedisTemplate.opsForValue().increment(viralityKey(postId), interactionType.getScore());

    }

    /* Virality key */
    private String viralityKey(Long postId){

        return "postid: "+ postId +" :viralityScore";
    }
    /* botCountkey */
    private String botCountKey(Long postId){
        return "postid: "+ postId +" :botCount";
    }
    /* Cool downKey */
    private String coolDownKey(Long botId, Long humanId){
        return "cooldown:bot_" + botId + ":human_" + humanId;
    }


    /* we will create one record which will store the details used for rollback */
    public record botCommentReservation(String botCountKey, String coolDownKey, boolean coolDownReserved){

    }

    /* Method to resolve the comment by a bot, means  checking can bot comment*/
    public botCommentReservation reservedBotComment(Long postId, Long botId, Long humanId){

        String botCountKey = botCountKey(postId); // create the bot count post id means postid = 5
        Long botCount =  stringRedisTemplate.opsForValue().increment(botCountKey);  // Increase the botcount , atomic increase

        // check the botCount 
        if(botCount == null){
            throw new IllegalArgumentException("Unable to reserve the bot reply in the redis");
        }

        // Now check bot Count is greater than BotReply cap then decrement the botcount and reject it
        if(botCount > BOT_REPLY_CAP){
            // Then we do decrement the bot count and reject the requserRepository
            stringRedisTemplate.opsForValue().decrement(botCountKey);
            throw new TooManyRequestException("bot reply limit reached for post id: "+ postId);
        }


        String coolDownKey = null;
        boolean coolDownReserved = false;


        /* Bot is interacting with human so we need to check cooldown period , ttl  */
        if(humanId != null){
            coolDownKey = coolDownKey(postId, humanId); // first create the cooldownkey

            /*create the cooldown period , if cooldown period does not exists */
          boolean acquired  = stringRedisTemplate.opsForValue().setIfAbsent(coolDownKey, "1", BOT_HUMAN_DURATION);


          /* if acquired is false means cooldown period is already active for this post */
          if(Boolean.FALSE.equals(acquired)){
              // decrement the redis bot count
              stringRedisTemplate.opsForValue().decrement(botCountKey);
              throw new TooManyRequestException("Bot:" + botId+" already in cool down period");

            }

          coolDownReserved = Boolean.TRUE.equals(acquired);
        }
        return new botCommentReservation(botCountKey, coolDownKey, coolDownReserved);
    }


    /* Method to validate the depth level , means if bot has depth level greater than 20 then we reject it */
    public void validateDepthLevel(int depthLevel, AuthorType authorType) throws BadRequestException{

        if(depthLevel > 20 && authorType == AuthorType.BOT){
            
            throw new BadRequestException("Bot reply cannot exceed 20 levels");

        }
    }

    /* Method for Rollback , we decrement the bot count and delete the coolDownKey */
    public void rollBack(botCommentReservation reservation){

        if(reservation == null){
            return;
        }

        stringRedisTemplate.opsForValue().decrement(reservation.botCountKey);

        if(reservation.coolDownKey() != null && reservation.coolDownReserved()){

            // delete the coolDownKey
            stringRedisTemplate.delete(reservation.coolDownKey());
        }
    }







    
}
