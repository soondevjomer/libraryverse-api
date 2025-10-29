package com.soondevjomer.libraryverse.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "cart")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Many cart items per user
    @JoinColumn(name = "user_id", nullable = false) // FK column
    private User cartBy;

    @ManyToOne(fetch = FetchType.LAZY) // Many cart items per book
    @JoinColumn(name = "book_id", nullable = false) // FK column
    private Book cartedBook;

    private Integer quantity;
}
