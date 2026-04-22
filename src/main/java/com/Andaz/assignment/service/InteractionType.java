package com.Andaz.assignment.service;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public enum InteractionType {

    BOT_REPLY(1L),
    HUMAN_LIKE(20L),
    HUMAN_COMMENT(50L);

    private final Long score;

    /* I will need the score to compare */
    public Long getScore(){
        return score;
    }
    
}
