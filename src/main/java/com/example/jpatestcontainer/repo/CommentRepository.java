package com.example.jpatestcontainer.repo;

import com.example.jpatestcontainer.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {}

