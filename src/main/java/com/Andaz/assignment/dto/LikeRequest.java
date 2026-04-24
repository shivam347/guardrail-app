package com.Andaz.assignment.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LikeRequest {

 // post id is sending in the api itself as {postid}, so there is no need to send post id explicitly

    @NotNull
    private Long userId;  // WHO LIKE THE POST
    
}
