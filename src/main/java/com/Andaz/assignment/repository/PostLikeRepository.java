package com.Andaz.assignment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Andaz.assignment.entity.PostLike;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long>{

    
    Long countByPostId(Long postId);

    boolean existsByPostIdAndUserId(Long postId, Long userId);


    
}
