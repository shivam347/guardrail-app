package com.Andaz.assignment.service;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class NotificationService {

    private final StringRedisTemplate redisTemplate;

    private final Duration NOTIFICATION_COOLDOWN = Duration.ofMinutes(15);

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    // Global Pending Notification
    private static final String PENDING_USERS_KEY = "pending_notif_users";

    /*
     * Method to handle the bot interaction , for which i need to pass userid which
     * comment the bot is interacting
     * and the notification message
     */
    public void handleBotInteraction(Long userId, String notificationMessage) {
        // first create the cooldownkey
        String coolDownKey = notificationCoolDownKey(userId);
        Boolean sentNotification = redisTemplate.opsForValue().setIfAbsent(coolDownKey, "1", NOTIFICATION_COOLDOWN);

        /* if sentnotification is equals true then we send the notification */
        if (Boolean.TRUE.equals(sentNotification)) {
            log.info("Push Notification Sent to user {}:{}", userId, notificationMessage);
            return;
        }

        /* Then create the pendingNotification key */
        String pendingKey = pendingNotificationKey(userId);
        // store the notification message into the redis
        redisTemplate.opsForList().rightPush(pendingKey, notificationMessage); // {"msg1", "msg2"}
        // store the set of userid , to which the notification to be sent
        redisTemplate.opsForSet().add(PENDING_USERS_KEY, String.valueOf(userId));

    }

    /* Method which will process all the pending notification in every 5 minutes */
    @Scheduled(fixedRate = 300000)
    public void processNotifications() {
        // First fetch all the users from the pending list
        Set<String> userIds = redisTemplate.opsForSet().members(PENDING_USERS_KEY);
        // if userId is null or empty then we simply return
        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        // Now we process every userId
        for (String userId : userIds) {
            String pendingListKey = pendingNotificationKey(Long.valueOf(userId));
            List<String> pendingMessages = redisTemplate.opsForList().range(pendingListKey, 0, -1);

            if (pendingMessages == null || pendingMessages.isEmpty()) {
                redisTemplate.opsForSet().remove(PENDING_USERS_KEY, userId);
                continue;
            }

            redisTemplate.delete(pendingListKey);
            redisTemplate.opsForSet().remove(PENDING_USERS_KEY, userId);

            String firstActor = extractBotLabel(pendingMessages.get(0));
            int otherCount = Math.max(pendingMessages.size() - 1, 0);
            log.info(
                    "Summarized Push Notification for User {}: {} and {} others interacted with your posts.",
                    userId,
                    firstActor,
                    otherCount);
        }
    }

    private String extractBotLabel(String notificationMessage) {
        int repliedIndex = notificationMessage.indexOf(" replied");
        if (repliedIndex > 0) {
            return notificationMessage.substring(0, repliedIndex);
        }
        return notificationMessage;
    }

    private String pendingNotificationKey(Long userId) {

        return "userId: " + userId + " :pending_notification";
    }

    private String notificationCoolDownKey(Long userId) {

        return "user: " + userId + ": notif_cooldown";

    }

}
