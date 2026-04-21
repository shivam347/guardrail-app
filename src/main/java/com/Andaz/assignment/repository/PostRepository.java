package com.Andaz.assignment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Andaz.assignment.entity.Post;

@Repository
public interface PostRepository extends JpaRepository<Post,Long> {


    
}
