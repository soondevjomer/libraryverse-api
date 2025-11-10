package com.soondevjomer.libraryverse.repository;

import com.soondevjomer.libraryverse.model.Library;
import com.soondevjomer.libraryverse.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface LibraryRepository extends JpaRepository<Library, Long>, JpaSpecificationExecutor<Library> {

    Optional<Library> findByOwnerUsername(String username);

    @Query(value = """
    SELECT MAX(popularity_score) AS max_popularity
    FROM (
        SELECT 
            l.library_id,
            (COALESCE(l.view_count, 0) * 0.1) +
            (COALESCE(SUM(so.subtotal), 0) * 0.6) +
            (COALESCE(SUM(i.delivered), 0) * 0.3) AS popularity_score
        FROM libraries l
        LEFT JOIN store_order so ON so.library_id = l.library_id
        LEFT JOIN book b ON b.library_id = l.library_id
        LEFT JOIN inventory i ON i.inventory_id = b.inventory_id
        WHERE (so.payment_status = 'PAID' AND so.order_status = 'DELIVERED')
           OR so.store_order_id IS NULL
        GROUP BY l.library_id
    ) AS scores
    """, nativeQuery = true)
    Double findMaxPopularityScore();

}

