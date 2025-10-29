package com.soondevjomer.libraryverse.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long id;

    @CreationTimestamp
    private LocalDateTime membershipDate;

    // FOR DELIVERY PURPOSES
    private String address;
    private String contactNumber;

    @OneToOne()
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
