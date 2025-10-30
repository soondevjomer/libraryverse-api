package com.soondevjomer.libraryverse.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "libraries")
public class Library {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "library_id")
    private Long id;

    @CreationTimestamp
    private LocalDateTime createdDate;

    @Column(nullable = false)
    private String name;

    private String address;
    private String contactNumber;
    private String description;

    @OneToMany(mappedBy = "library", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Book> books = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "owner_id", unique = true)
    private User owner;

    @OneToMany(mappedBy = "library", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreOrder> storeOrders = new ArrayList<>();

    private Long viewCount = 0L;
    private String libraryCover;

}