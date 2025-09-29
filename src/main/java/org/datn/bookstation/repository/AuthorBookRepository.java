package org.datn.bookstation.repository;

import org.datn.bookstation.entity.AuthorBook;
import org.datn.bookstation.entity.AuthorBookId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorBookRepository extends JpaRepository<AuthorBook, AuthorBookId> {
    
    @Query("SELECT ab FROM AuthorBook ab JOIN FETCH ab.author WHERE ab.book.id = :bookId")
    List<AuthorBook> findByBookIdWithAuthor(@Param("bookId") Integer bookId);
    
    @Query("SELECT ab FROM AuthorBook ab WHERE ab.book.id = :bookId")
    List<AuthorBook> findByBookId(@Param("bookId") Integer bookId);
    
    @Query("SELECT ab FROM AuthorBook ab WHERE ab.author.id = :authorId")
    List<AuthorBook> findByAuthorId(@Param("authorId") Integer authorId);
    
    /**
     * Lấy thông tin authors cho nhiều books cùng lúc (để tránh N+1 query)
     */
    @Query("SELECT ab FROM AuthorBook ab JOIN FETCH ab.author WHERE ab.book.id IN :bookIds")
    List<AuthorBook> findByBookIdsWithAuthor(@Param("bookIds") List<Integer> bookIds);
    
    void deleteByBookId(Integer bookId);
}
