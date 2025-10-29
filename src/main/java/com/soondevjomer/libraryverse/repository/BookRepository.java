package com.soondevjomer.libraryverse.repository;

import com.soondevjomer.libraryverse.model.Book;
import com.soondevjomer.libraryverse.model.Library;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    long countByLibrary(Library library);

    @Query("SELECT COUNT(b) FROM Book b JOIN b.inventory i WHERE b.library = :library AND i.availableStock <= :threshold")
    long countLowStockBooks(@Param("library") Library library, @Param("threshold") int threshold);

    @Query("SELECT MAX(b.viewCount) FROM Book b")
    Double findMaxViewCount();

    @Query("""
      SELECT MAX((COALESCE(b.viewCount, 0) * 0.2) + 
                 (COALESCE(i.delivered, 0) * 0.8))
      FROM Book b
      LEFT JOIN b.inventory i
    """)
    Double findMaxPopularityScore();

}
