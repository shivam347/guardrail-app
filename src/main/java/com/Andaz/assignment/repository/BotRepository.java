package com.Andaz.assignment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Andaz.assignment.entity.Bot;

public interface BotRepository extends JpaRepository<Bot, Long> {

    boolean existsByName(String name);
    
}
