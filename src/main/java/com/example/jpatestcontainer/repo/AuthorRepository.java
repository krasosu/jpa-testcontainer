package com.example.jpatestcontainer.repo;

import com.example.jpatestcontainer.domain.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, Long> {}

