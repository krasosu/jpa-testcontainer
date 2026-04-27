package com.example.jpatestcontainer.repo;

import com.example.jpatestcontainer.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {}

