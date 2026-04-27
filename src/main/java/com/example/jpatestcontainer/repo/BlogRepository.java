package com.example.jpatestcontainer.repo;

import com.example.jpatestcontainer.domain.Blog;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface BlogRepository extends JpaRepository<Blog, Long> {
  @EntityGraph(attributePaths = {"posts", "authors", "tags", "comments"})
  Optional<Blog> findWithChildrenById(Long id);
}

