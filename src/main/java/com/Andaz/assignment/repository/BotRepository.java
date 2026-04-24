package com.Andaz.assignment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Andaz.assignment.entity.Bot;

@Repository
public interface BotRepository extends JpaRepository<Bot, Long> {

    boolean existsByName(String name);
    
}
