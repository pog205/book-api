package org.datn.bookstation.repository;

import org.datn.bookstation.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface AuthorRepository extends JpaRepository<Author, Integer>, JpaSpecificationExecutor<Author> {
    List<Author> findByStatus(Byte status);

    boolean existsByAuthorNameIgnoreCase(String authorName);

    Author findByAuthorNameIgnoreCase(String authorName);
}
