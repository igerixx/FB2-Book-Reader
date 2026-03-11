package com.Repository;

import com.Entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    boolean existsById(Long id);
    boolean existsByFilenameAndUserId(String filename, Long id);
    List<Book> findAllByUserId(Long id);
    Optional<Book> findById(Long id);
}
