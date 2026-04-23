package com.Andaz.assignment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Andaz.assignment.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {


    
}
