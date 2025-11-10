package com.soondevjomer.libraryverse.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "book")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Long id;

    @Column(name = "isbn", nullable = false)
    private String isbn;

    @CreationTimestamp
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "book_detail_id", referencedColumnName = "book_detail_id")
    private BookDetail bookDetail;

    @ManyToOne
    @JoinColumn(name = "library_id", referencedColumnName = "library_id")
    private Library library;

    @OneToMany(mappedBy = "cartedBook", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Cart> carts;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", referencedColumnName = "inventory_id")
    private Inventory inventory;

    @Column(name = "view_count")
    private Long viewCount;
}
