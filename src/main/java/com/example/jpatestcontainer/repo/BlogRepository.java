package com.example.jpatestcontainer.repo;

import com.example.jpatestcontainer.domain.Blog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogRepository extends JpaRepository<Blog, Long> {}

