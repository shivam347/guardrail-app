package com.Andaz.assignment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Andaz.assignment.entity.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {


    
}
