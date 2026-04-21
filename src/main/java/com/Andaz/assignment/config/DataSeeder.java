package com.Andaz.assignment.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.Andaz.assignment.entity.Bot;
import com.Andaz.assignment.entity.User;
import com.Andaz.assignment.repository.BotRepository;
import com.Andaz.assignment.repository.UserRepository;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class DataSeeder implements CommandLineRunner {

    // Need user repo and bot repo
    private final UserRepository userRepo;
    private final BotRepository botRepo;

    @Override
    public void run(String... args) {
       seedUser();
       seedBot();
    }


        private void seedUser() {
        if (!userRepo.existsByUsername("vidhi")) {
            User user = new User();
            user.setUsername("vidhi");
            user.setPremium(true);
            userRepo.save(user);
        }
    }

    private void seedBot() {
        if (!botRepo.existsByName("trendbot")) {
            Bot bot = new Bot();
            bot.setName("trendbot");
            bot.setPersonaDescription("A casual and engaging bot");
            botRepo.save(bot);
        }
    }
    
}
